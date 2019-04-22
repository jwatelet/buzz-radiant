package be.jwa.actors

import java.util.UUID

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill, Props}
import akka.pattern.{ask, pipe}
import akka.stream.scaladsl.{RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.util.Timeout
import be.jwa.ConfigTwitterCredentials
import be.jwa.actors.BuzzActor._
import be.jwa.actors.TweetGraphActor.{MakeGraph, MakeTwitterSource}
import be.jwa.actors.TwitterActor.{GetStatistics, TwitterMessage}
import be.jwa.controllers.TwitterStatistics
import be.jwa.flows.ParserStatus
import be.jwa.json.TwitterJsonSupport
import be.jwa.sources.SourceAndTwitterClient
import com.typesafe.config.ConfigFactory
import spray.json._
import twitter4j.{Status, TwitterStream}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

case class BuzzObserverId(hashtags: Seq[String], id: UUID)

case class BuzzObserver(hashtags: Seq[String], twitterActor: ActorRef, killSwitch: UniqueKillSwitch,
                        eventualTwitterStream: Future[TwitterStream], source: Source[Status, NotUsed])

object BuzzActor {

  trait BuzzMessage

  case class InitStatisticWebsocket(id: UUID, websocketEntry: ActorRef) extends BuzzMessage

  case class StopStatisticWebsocket(id: UUID) extends BuzzMessage

  case class InitTweetWebsocket(id: UUID, websocketEntry: ActorRef) extends BuzzMessage

  case object GetAllBuzzObserversIds extends BuzzMessage

  case class CreateBuzzObserver(hashtags: Seq[String]) extends BuzzMessage

  case class GetOrCreateWebsocket(id: UUID) extends BuzzMessage

  case class StopBuzzObserver(id: UUID) extends BuzzMessage

  case class DeleteBuzzObserver(id: UUID) extends BuzzMessage

  case class SendMessageToTwitterActor(id: UUID, msg: TwitterMessage) extends BuzzMessage


  def props()(implicit materializer: ActorMaterializer, timeout: Timeout, ec: ExecutionContext) = Props(new BuzzActor())
}

class BuzzActor(implicit val timeout: Timeout, implicit val materializer: ActorMaterializer, implicit val ec: ExecutionContext) extends Actor with ActorLogging with TwitterJsonSupport {

  private val credentials = new ConfigTwitterCredentials(ConfigFactory.load("twitter.conf"))
  private val graphActor = context.actorOf(TweetGraphActor.props(), "graph-actor")
  private var buzzObserverMap = Map[UUID, BuzzObserver]()
  private var statisticsSchedulerMap = Map[UUID, Cancellable]()

  def receive: Receive = {

    case InitStatisticWebsocket(id, websocketEntry) =>
      log.info(s"InitStatisticWebSocket")
      val cancellable: Cancellable = context.system.scheduler.schedule(0.milliseconds, 3.seconds) {
        (self ? SendMessageToTwitterActor(id, GetStatistics(10)))
          .mapTo[Option[TwitterStatistics]]
          .map(maybeStatistics => maybeStatistics.foreach(s => websocketEntry ! s.toJson.toString()))
      }
      statisticsSchedulerMap += (id -> cancellable)
      sender() ! s"Initialisation of Statistic for observer : $id"

    case StopStatisticWebsocket(id) =>
      statisticsSchedulerMap.get(id).foreach { c =>
        c.cancel()
        statisticsSchedulerMap -= id

        log.info(s"StopStatisticWebsocket id : $id")
      }

    case CreateBuzzObserver(hashtags) =>
      log.info(s"CreateBuzzObserver : $hashtags")
      createBuzzObserver(hashtags) pipeTo sender

    case StopBuzzObserver(id) =>
      stopBuzzObserver(id)
      log.info(s"StopBuzzObserver id : $id")

    case DeleteBuzzObserver(id) =>
      deleteBuzzObserver(id)
      log.info(s"DeleteBuzzObserver id : $id")
      sender() ! "Deletion launched"

    case GetAllBuzzObserversIds =>
      val observerIds: Seq[BuzzObserverId] = buzzObserverMap.map { case (id, observer) =>
        BuzzObserverId(observer.hashtags, id)
      }.toSeq
      log.info(s"GetAllBuzzObserversIds")
      sender ! observerIds

    case SendMessageToTwitterActor(id, msg) =>
      val response: Future[Option[Any]] = buzzObserverMap.get(id).map(bo => bo.twitterActor ? msg)
      log.info(s"ask twitter actor $id msg : $msg")
      response pipeTo sender

    case InitTweetWebsocket(id, wsEntry) =>
      log.info(s"InitTweetWebSocket")
      buzzObserverMap.get(id).foreach { o =>
        o.source
          .via(ParserStatus.parse)
          .map(t => wsEntry ! t.toJson.toString)
          .runWith(Sink.ignore)
      }

    case msg => log.error(s"Unknown received message : $msg")
  }

  private def deleteBuzzObserver(id: UUID): Unit = {
    buzzObserverMap.get(id).foreach { bo =>
      bo.killSwitch.shutdown()
      bo.eventualTwitterStream.foreach(ts => ts.shutdown())
      bo.twitterActor ! PoisonPill
    }
    buzzObserverMap = buzzObserverMap.filterKeys(k => k != id)
  }

  private def stopBuzzObserver(id: UUID): Unit = {
    buzzObserverMap.get(id).foreach { bo =>
      bo.killSwitch.shutdown()
      bo.eventualTwitterStream.foreach(ts => ts.shutdown())
    }
  }

  private def createBuzzObserver(hashtags: Seq[String]): Future[UUID] = {
    val uuid = UUID.randomUUID()
    log.info(s"Start buzz observer creation uuid : $uuid")
    val twitterActor: ActorRef = context.actorOf(TwitterActor.props(hashtags), s"tweet-actor-$uuid")
    for {
      uuid <- Future(UUID.randomUUID())
      sourceAndTwitterClient <- (graphActor ? MakeTwitterSource(credentials, hashtags)).mapTo[SourceAndTwitterClient]
      graph <- (graphActor ? MakeGraph(sourceAndTwitterClient.source, twitterActor)).mapTo[RunnableGraph[UniqueKillSwitch]]
    } yield {
      val ks = graph.run()
      buzzObserverMap += (uuid -> BuzzObserver(hashtags, twitterActor, ks, sourceAndTwitterClient.eventualTwitterClient, sourceAndTwitterClient.source))
      uuid
    }
  }


  implicit def optionToFuture[A](x: Option[Future[A]])(implicit ec: ExecutionContext): Future[Option[A]] =
    x match {
      case Some(f) => f.map(Some(_))
      case None => Future.successful(None)
    }
}
