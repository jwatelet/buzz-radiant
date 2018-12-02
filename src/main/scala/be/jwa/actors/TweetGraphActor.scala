package be.jwa.actors

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import be.jwa.ConfigTwitterCredentials
import be.jwa.actors.TweetGraphActor.{MakeGraph, MakeTwitterSource}
import be.jwa.controllers.Tweet
import be.jwa.controllers.graph.{TwitterGraphMaker, TwitterSourceMaker}
import org.slf4j.LoggerFactory


object TweetGraphActor {

  trait TweetMessage

  case class MakeGraph(twitterSource: Source[Tweet, NotUsed], tweetActor: ActorRef) extends TweetMessage

  case class MakeTwitterSource(credentials: ConfigTwitterCredentials, hashtags: Seq[String]) extends TweetMessage

  def props()(implicit materializer: ActorMaterializer, timeout: Timeout): Props = Props(new TweetGraphActor())

}

class TweetGraphActor(implicit val timeout: Timeout, implicit val materializer: ActorMaterializer) extends Actor with TwitterGraphMaker with TwitterSourceMaker {

  private val logger = LoggerFactory.getLogger(getClass.getName)
  implicit val system: ActorSystem = context.system

  def receive: Receive = {

    case MakeGraph(twitterSource, tweetActor) =>
      logger.info(s"MakeGraph")
      sender() ! makeTwitterGraph(twitterSource, tweetActor)

    case MakeTwitterSource(credentials, hashtags) =>
      logger.info(s"MakeTwitterSource")
      sender() ! makeTwitterSource(credentials, hashtags)

    case msg => logger.error(s"Unknown received message : $msg")
  }
}
