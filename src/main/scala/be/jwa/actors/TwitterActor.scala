package be.jwa.actors

import akka.actor.{Actor, Props}
import be.jwa.actors.TwitterActor._
import be.jwa.controllers.Tweet

import scala.collection.mutable.ListBuffer

case class TweetCount(count: Int)

case class TwitterUserCount(count: Int)

object TwitterActor {

  case class AddTweet(tweet: Tweet)

  case object GetTweetCount

  case object GetTweets

  case object GetUsers

  case object GetUsersCount

  def props(): Props = Props(new TwitterActor())
}

class TwitterActor extends Actor {

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
      sender() ! tweetBuffer.map(tweet => tweet.user)

    case GetUsersCount =>
      sender() ! TwitterUserCount(tweetBuffer.map(tweet => tweet.user).length)
  }
}
