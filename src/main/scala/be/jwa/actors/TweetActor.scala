package be.jwa.actors

import akka.actor.{Actor, Props}
import be.jwa.actors.TweetActor.{AddTweet, GetTweetCount, GetTweets}
import be.jwa.sources.Tweet

import scala.collection.mutable.ListBuffer

case class TweetCount(count: Int)

object TweetActor {

  case class AddTweet(tweet: Tweet)

  case object GetTweetCount

  case object GetTweets

  def props(): Props = Props(new TweetActor())
}

class TweetActor extends Actor {

  private val tweetBuffer = ListBuffer[Tweet]()

  def receive: Receive = {

    case AddTweet(tweet: Tweet) =>
      tweetBuffer.+=(tweet)
      sender() ! s" ${tweet.id} Added"

    case GetTweetCount =>
      sender() ! TweetCount(tweetBuffer.length)

    case GetTweets =>
      sender() ! tweetBuffer
  }
}
