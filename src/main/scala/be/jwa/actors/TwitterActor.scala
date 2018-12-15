package be.jwa.actors

import akka.actor.{Actor, ActorLogging, Props}
import be.jwa.actors.TwitterActor._
import be.jwa.controllers.{StatisticsMaker, Tweet}

import scala.collection.mutable.ListBuffer

case class TweetCount(count: Int)

case class TwitterUserCount(count: Int)

case class PlaceCount(count: Int)

case class GeolocationCount(count: Int)

object TwitterActor {

  trait TwitterMessage

  case class AddTweet(tweet: Tweet) extends TwitterMessage

  case object GetStatistics extends TwitterMessage

  case object GetTweets extends TwitterMessage

  case object GetTweetCount extends TwitterMessage

  case object GetUsers extends TwitterMessage

  case object GetUsersCount extends TwitterMessage

  case object GetPlaces extends TwitterMessage

  case object GetPlaceCount extends TwitterMessage

  case object GetGeolocations extends TwitterMessage

  case object GetGeolocationCount extends TwitterMessage

  def props(): Props = Props(new TwitterActor())
}

class TwitterActor extends Actor with ActorLogging with StatisticsMaker {

  val tweetBuffer: ListBuffer[Tweet] = ListBuffer()

  def receive: Receive = {

    case AddTweet(tweet: Tweet) =>
      tweetBuffer.+=(tweet)
      log.info(s"AddTweet : ${tweet.id} Added")

    case GetStatistics =>

      sender() ! makeStatistics(tweetBuffer)

    case GetTweetCount =>
      log.info(s"Get TweetCount")
      sender() ! TweetCount(tweetBuffer.length)

    case GetTweets =>
      log.info(s"GetTweets")
      sender() ! tweetBuffer.toList.takeRight(1000)

    case GetUsers =>
      log.info(s"GetUsers")
      sender() ! tweetBuffer.map(tweet => tweet.user).toSet.takeRight(1000)

    case GetUsersCount =>
      log.info(s"GetUsersCount")
      sender() ! TwitterUserCount(tweetBuffer.map(tweet => tweet.user).toSet.size)

    case GetPlaces =>
      log.info(s"GetPlaces")
      sender() ! tweetBuffer.filter(t => t.place.isDefined)
        .flatMap(t => t.place)

    case GetPlaceCount =>
      log.info(s"GetPlaceCount")
      sender() ! PlaceCount(tweetBuffer.toList.count(t => t.place.isDefined))

    case GetGeolocations =>
      sender() ! tweetBuffer.filter(t => t.geolocation.isDefined)
        .flatMap(t => t.geolocation)

      log.info(s"GetGeolocations")

    case GetGeolocationCount =>
      log.info(s"GetGeolocationCount")
      sender() ! PlaceCount(tweetBuffer.toList.count(t => t.geolocation.isDefined))

    case msg => log.error(s"Unknown received message : $msg")
  }
}
