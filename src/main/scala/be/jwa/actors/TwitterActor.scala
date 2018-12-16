package be.jwa.actors

import akka.actor.{Actor, ActorLogging, Props}
import be.jwa.actors.TwitterActor._
import be.jwa.controllers.{StatisticsMaker, Tweet}
import akka.pattern.pipe
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

case class PlaceCount(count: Int)

case class GeolocationCount(count: Int)

object TwitterActor {

  trait TwitterMessage

  case class AddTweet(tweet: Tweet) extends TwitterMessage

  case object GetStatistics extends TwitterMessage

  case object GetTweets extends TwitterMessage

  case object GetUsers extends TwitterMessage

  case object GetPlaces extends TwitterMessage

  case object GetPlaceCount extends TwitterMessage

  case object GetGeolocations extends TwitterMessage

  case object GetGeolocationCount extends TwitterMessage

  def props(hashtags: Seq[String])(implicit ec: ExecutionContext): Props = Props(new TwitterActor(hashtags))
}

class TwitterActor(val hashtags: Seq[String])(implicit val ec: ExecutionContext) extends Actor with ActorLogging with StatisticsMaker {

  val tweetBuffer: ListBuffer[Tweet] = ListBuffer()

  def receive: Receive = {

    case AddTweet(tweet: Tweet) =>
      tweetBuffer.+=(tweet)
      log.debug(s"AddTweet : ${tweet.id} Added")

    case GetStatistics =>
      log.debug(s"GetStatistics")
      makeStatistics(tweetBuffer) pipeTo sender()

    case GetTweets =>
      log.debug(s"GetTweets")
      sender() ! tweetBuffer.toList.takeRight(1000)

    case GetUsers =>
      log.debug(s"GetUsers")
      sender() ! tweetBuffer.map(tweet => tweet.user).toSet.takeRight(1000)

    case GetPlaces =>
      log.debug(s"GetPlaces")
      sender() ! tweetBuffer.filter(t => t.place.isDefined)
        .flatMap(t => t.place)

    case GetPlaceCount =>
      log.debug(s"GetPlaceCount")
      sender() ! PlaceCount(tweetBuffer.toList.count(t => t.place.isDefined))

    case GetGeolocations =>
      log.debug(s"GetGeolocations")
      sender() ! tweetBuffer.filter(t => t.geolocation.isDefined)
        .flatMap(t => t.geolocation)

    case GetGeolocationCount =>
      log.debug(s"GetGeolocationCount")
      sender() ! PlaceCount(tweetBuffer.toList.count(t => t.geolocation.isDefined))

    case msg => log.error(s"Unknown received message : $msg")
  }
}
