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

  def props()(implicit ec: ExecutionContext): Props = Props(new TwitterActor())
}

class TwitterActor(implicit val ec: ExecutionContext) extends Actor with ActorLogging with StatisticsMaker {

  val tweetBuffer: ListBuffer[Tweet] = ListBuffer()

  def receive: Receive = {

    case AddTweet(tweet: Tweet) =>
      tweetBuffer.+=(tweet)
      log.info(s"AddTweet : ${tweet.id} Added")

    case GetStatistics =>
      log.info(s"GetStatistics")
      makeStatistics(tweetBuffer) pipeTo sender()

    case GetTweets =>
      log.info(s"GetTweets")
      sender() ! tweetBuffer.toList.takeRight(1000)

    case GetUsers =>
      log.info(s"GetUsers")
      sender() ! tweetBuffer.map(tweet => tweet.user).toSet.takeRight(1000)

    case GetPlaces =>
      log.info(s"GetPlaces")
      sender() ! tweetBuffer.filter(t => t.place.isDefined)
        .flatMap(t => t.place)

    case GetPlaceCount =>
      log.info(s"GetPlaceCount")
      sender() ! PlaceCount(tweetBuffer.toList.count(t => t.place.isDefined))

    case GetGeolocations =>
      log.info(s"GetGeolocations")
      sender() ! tweetBuffer.filter(t => t.geolocation.isDefined)
        .flatMap(t => t.geolocation)

    case GetGeolocationCount =>
      log.info(s"GetGeolocationCount")
      sender() ! PlaceCount(tweetBuffer.toList.count(t => t.geolocation.isDefined))

    case msg => log.error(s"Unknown received message : $msg")
  }
}
