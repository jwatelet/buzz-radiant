package be.jwa.actors

import akka.NotUsed
import akka.actor.{Actor, ActorRef, Props}
import akka.stream.scaladsl.Source
import akka.util.Timeout
import be.jwa.actors.TweetGraphActor.MakeGraph
import be.jwa.controllers.Tweet
import be.jwa.controllers.graph.TwitterGraphMaker


object TweetGraphActor {

  case class MakeGraph(twitterSource: Source[Tweet, NotUsed], tweetActor: ActorRef)

  def props()(implicit timeout : Timeout): Props = Props(new TweetGraphActor())

}

class TweetGraphActor(implicit val timeout : Timeout) extends Actor with TwitterGraphMaker {
  def receive: Receive = {

    case MakeGraph(twitterSource, tweetActor) =>
      sender() ! makeTwitterGraph(twitterSource, tweetActor)
  }
}
