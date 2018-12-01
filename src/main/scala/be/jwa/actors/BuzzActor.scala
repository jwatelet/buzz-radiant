package be.jwa.actors

import java.util.UUID

import akka.NotUsed
import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{RunnableGraph, Source}
import akka.util.Timeout
import be.jwa.ConfigTwitterCredentials
import be.jwa.actors.BuzzActor.CreateBuzzObserver
import be.jwa.actors.TweetGraphActor.{MakeGraph, MakeTwitterSource}
import be.jwa.controllers.Tweet
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

object BuzzActor {

  trait BuzzMessage

  case class CreateBuzzObserver(hashtags: Seq[String]) extends BuzzMessage

  def props()(implicit materializer: ActorMaterializer, timeout: Timeout, ec: ExecutionContext) = Props(new BuzzActor())
}

class BuzzActor(implicit val timeout: Timeout, implicit val materializer: ActorMaterializer, implicit val ec: ExecutionContext) extends Actor {


  private val logger = LoggerFactory.getLogger(getClass.getName)
  private var twitterActorMap = Map[UUID, ActorRef]()
  private val graphActor = context.actorOf(TweetGraphActor.props(), "graph-actor")
  private val credentials = new ConfigTwitterCredentials(ConfigFactory.load())

  def receive: Receive = {

    case CreateBuzzObserver(hashtags) =>
      createBuzzObserver(hashtags) pipeTo sender

    case msg => logger.error(s"Unknown received message : $msg")
  }

  def createBuzzObserver(hashtags: Seq[String]): Future[UUID] = {

    val uuid = UUID.randomUUID()
    logger.info(s"Start buzz observer creation uuid : $uuid")
    val twitterActor: ActorRef = context.actorOf(TwitterActor.props(), s"tweet-actor-$uuid")
    for {
      uuid <- Future(UUID.randomUUID())
      source <- (graphActor ? MakeTwitterSource(credentials, hashtags)).mapTo[Source[Tweet, NotUsed]]
      graph <- (graphActor ? MakeGraph(source, twitterActor)).mapTo[RunnableGraph[NotUsed]]
    } yield {
      graph.run()
      twitterActorMap += (uuid -> twitterActor)
      uuid
    }
  }
}
