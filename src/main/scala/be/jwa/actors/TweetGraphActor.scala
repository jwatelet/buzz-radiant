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


object TweetGraphActor {

  case class MakeGraph(twitterSource: Source[Tweet, NotUsed], tweetActor: ActorRef)

  case class MakeTwitterSource(credentials: ConfigTwitterCredentials, hashtags: Seq[String])

  def props()(implicit materializer: ActorMaterializer, timeout: Timeout): Props = Props(new TweetGraphActor())

}

class TweetGraphActor(implicit val timeout: Timeout, implicit val materializer: ActorMaterializer) extends Actor with TwitterGraphMaker with TwitterSourceMaker {

  implicit val system: ActorSystem = context.system

  def receive: Receive = {

    case MakeGraph(twitterSource, tweetActor) =>
      sender() ! makeTwitterGraph(twitterSource, tweetActor)

    case MakeTwitterSource(credentials, hashtags) =>
      sender() ! makeTwitterSource(credentials, hashtags)
  }
}
