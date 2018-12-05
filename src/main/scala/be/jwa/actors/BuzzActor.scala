package be.jwa.actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.pattern.{ask, pipe}
import akka.stream.scaladsl.RunnableGraph
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.util.Timeout
import be.jwa.ConfigTwitterCredentials
import be.jwa.actors.BuzzActor._
import be.jwa.actors.TweetGraphActor.{MakeGraph, MakeTwitterSource}
import be.jwa.actors.TwitterActor.TwitterMessage
import be.jwa.sources.SourceAndTwitterClient
import com.twitter.hbc.twitter4j.Twitter4jStatusClient
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

case class BuzzObserver(twitterActor: ActorRef, killSwitch: UniqueKillSwitch, eventualTwitterClient: Future[Twitter4jStatusClient])

object BuzzActor {

  trait BuzzMessage

  case object GetAllBuzzObserversIds extends BuzzMessage

  case class CreateBuzzObserver(hashtags: Seq[String]) extends BuzzMessage

  case class StopBuzzObserver(id: UUID) extends BuzzMessage

  case class DeleteBuzzObserver(id: UUID) extends BuzzMessage

  case class SendMessageToTwitterActor(id: UUID, msg: TwitterMessage) extends BuzzMessage


  def props()(implicit materializer: ActorMaterializer, timeout: Timeout, ec: ExecutionContext) = Props(new BuzzActor())
}

class BuzzActor(implicit val timeout: Timeout, implicit val materializer: ActorMaterializer, implicit val ec: ExecutionContext) extends Actor {

  private val logger = LoggerFactory.getLogger(getClass.getName)
  private var twitterActorMap = Map[UUID, BuzzObserver]()
  private val graphActor = context.actorOf(TweetGraphActor.props(), "graph-actor")
  private val credentials = new ConfigTwitterCredentials(ConfigFactory.load())

  def receive: Receive = {

    case CreateBuzzObserver(hashtags) =>
      createBuzzObserver(hashtags) pipeTo sender

    case StopBuzzObserver(id) =>
      stopBuzzObserver(id)
      logger.info(s"StopBuzzObserver id : $id")

    case DeleteBuzzObserver(id) =>
      deleteBuzzObserver(id)
      logger.info(s"DeleteBuzzObserver id : $id")
      sender() ! "Deletion launched"

    case GetAllBuzzObserversIds =>
      logger.info(s"GetAllBuzzObservers ids")
      sender ! twitterActorMap.keySet

    case SendMessageToTwitterActor(id, msg) =>
      val response: Future[Option[Any]] = twitterActorMap.get(id).map(bo => bo.twitterActor ? msg)
      logger.info(s"ask twitter actor $id msg : $msg")
      response pipeTo sender

    case msg => logger.error(s"Unknown received message : $msg")
  }

  private def deleteBuzzObserver(id: UUID): Unit = {
    twitterActorMap.get(id).foreach { bo =>
      bo.killSwitch.shutdown()
      bo.eventualTwitterClient.foreach(tw => tw.stop())
      bo.twitterActor ! PoisonPill
    }
    twitterActorMap = twitterActorMap.filterKeys(k => k != id)
  }

  private def stopBuzzObserver(id: UUID): Unit = {
    twitterActorMap.get(id).foreach { bo =>
      bo.killSwitch.shutdown()
      bo.eventualTwitterClient.foreach(tw => tw.stop())
    }
  }

  private def createBuzzObserver(hashtags: Seq[String]): Future[UUID] = {

    val uuid = UUID.randomUUID()
    logger.info(s"Start buzz observer creation uuid : $uuid")
    val twitterActor: ActorRef = context.actorOf(TwitterActor.props(), s"tweet-actor-$uuid")
    for {
      uuid <- Future(UUID.randomUUID())
      sourceAndTwitterClient <- (graphActor ? MakeTwitterSource(credentials, hashtags)).mapTo[SourceAndTwitterClient]
      graph <- (graphActor ? MakeGraph(sourceAndTwitterClient.source, twitterActor)).mapTo[RunnableGraph[UniqueKillSwitch]]
    } yield {
      val ks = graph.run()
      twitterActorMap += (uuid -> BuzzObserver(twitterActor, ks, sourceAndTwitterClient.eventualTwitterClient))
      uuid
    }
  }


  implicit def optionToFuture[A](x: Option[Future[A]])(implicit ec: ExecutionContext): Future[Option[A]] =
    x match {
      case Some(f) => f.map(Some(_))
      case None => Future.successful(None)
    }
}
