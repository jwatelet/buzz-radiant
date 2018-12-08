package be.jwa.actors

import java.util.UUID

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.pattern.{ask, pipe}
import akka.stream.scaladsl.{RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.util.Timeout
import be.jwa.ConfigTwitterCredentials
import be.jwa.actors.BuzzActor._
import be.jwa.actors.TweetGraphActor.{MakeGraph, MakeTwitterSource}
import be.jwa.actors.TwitterActor.TwitterMessage
import be.jwa.flows.ParserStatus
import be.jwa.json.TwitterJsonSupport
import be.jwa.sources.SourceAndTwitterClient
import com.twitter.hbc.twitter4j.Twitter4jStatusClient
import com.typesafe.config.ConfigFactory
import spray.json._
import twitter4j.Status

import scala.concurrent.{ExecutionContext, Future}

case class BuzzObserver(twitterActor: ActorRef, killSwitch: UniqueKillSwitch,
                        eventualTwitterClient: Future[Twitter4jStatusClient], source: Source[Status, NotUsed])

object BuzzActor {

  trait BuzzMessage

  case class InitWebsocket(id: UUID, websocketEntry: ActorRef) extends BuzzMessage

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

  def receive: Receive = {

    case CreateBuzzObserver(hashtags) =>
      createBuzzObserver(hashtags) pipeTo sender

    case StopBuzzObserver(id) =>
      stopBuzzObserver(id)
      log.info(s"StopBuzzObserver id : $id")

    case DeleteBuzzObserver(id) =>
      deleteBuzzObserver(id)
      log.info(s"DeleteBuzzObserver id : $id")
      sender() ! "Deletion launched"

    case GetAllBuzzObserversIds =>
      log.info(s"GetAllBuzzObservers ids")
      sender ! buzzObserverMap.keySet

    case SendMessageToTwitterActor(id, msg) =>
      val response: Future[Option[Any]] = buzzObserverMap.get(id).map(bo => bo.twitterActor ? msg)
      log.info(s"ask twitter actor $id msg : $msg")
      response pipeTo sender

    case InitWebsocket(id, wsEntry) =>
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
      bo.eventualTwitterClient.foreach(tw => tw.stop())
      bo.twitterActor ! PoisonPill
    }
    buzzObserverMap = buzzObserverMap.filterKeys(k => k != id)
  }

  private def stopBuzzObserver(id: UUID): Unit = {
    buzzObserverMap.get(id).foreach { bo =>
      bo.killSwitch.shutdown()
      bo.eventualTwitterClient.foreach(tw => tw.stop())
    }
  }

  private def createBuzzObserver(hashtags: Seq[String]): Future[UUID] = {
    val uuid = UUID.randomUUID()
    log.info(s"Start buzz observer creation uuid : $uuid")
    val twitterActor: ActorRef = context.actorOf(TwitterActor.props(), s"tweet-actor-$uuid")
    for {
      uuid <- Future(UUID.randomUUID())
      sourceAndTwitterClient <- (graphActor ? MakeTwitterSource(credentials, hashtags)).mapTo[SourceAndTwitterClient]
      graph <- (graphActor ? MakeGraph(sourceAndTwitterClient.source, twitterActor)).mapTo[RunnableGraph[UniqueKillSwitch]]
    } yield {
      val ks = graph.run()
      buzzObserverMap += (uuid -> BuzzObserver(twitterActor, ks, sourceAndTwitterClient.eventualTwitterClient, sourceAndTwitterClient.source))
      uuid
    }
  }


  implicit def optionToFuture[A](x: Option[Future[A]])(implicit ec: ExecutionContext): Future[Option[A]] =
    x match {
      case Some(f) => f.map(Some(_))
      case None => Future.successful(None)
    }
}
