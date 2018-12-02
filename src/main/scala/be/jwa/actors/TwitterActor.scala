package be.jwa.actors

import akka.actor.{Actor, Props}
import be.jwa.actors.TwitterActor._
import be.jwa.controllers.Tweet
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer

case class TweetCount(count: Int)

case class TwitterUserCount(count: Int)

object TwitterActor {

  trait TwitterMessage

  case class AddTweet(tweet: Tweet) extends TwitterMessage

  case object GetTweetCount extends TwitterMessage

  case object GetTweets extends TwitterMessage

  case object GetUsers extends TwitterMessage

  case object GetUsersCount extends TwitterMessage

  def props(): Props = Props(new TwitterActor())
}

class TwitterActor extends Actor {

  private val logger = LoggerFactory.getLogger(getClass.getName)
  private val tweetBuffer = ListBuffer[Tweet]()

  def receive: Receive = {

    case AddTweet(tweet: Tweet) =>
      tweetBuffer.+=(tweet)
      sender() ! s" ${tweet.id} Added"

    case GetTweetCount =>
      sender() ! TweetCount(tweetBuffer.length)

    case GetTweets =>
      sender() ! tweetBuffer

    case GetUsers =>
      sender() ! tweetBuffer.map(tweet => tweet.user).toSet

    case GetUsersCount =>
      sender() ! TwitterUserCount(tweetBuffer.map(tweet => tweet.user).toSet.size)

    case msg => logger.error(s"Unknown received message : $msg")
  }
}
