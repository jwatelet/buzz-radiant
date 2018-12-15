package be.jwa.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.jwa.actors.{GeolocationCount, PlaceCount, TweetCount, TwitterUserCount}
import be.jwa.controllers._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TwitterJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val timeStatisticsJsonFormat: RootJsonFormat[TimeStatistic] = jsonFormat2(TimeStatistic)
  implicit val twitterStatisticsJsonFormat: RootJsonFormat[TwitterStatistics] = jsonFormat1(TwitterStatistics)
  implicit val twitterUserJsonFormat: RootJsonFormat[TwitterUser] = jsonFormat7(TwitterUser)
  implicit val twitterPlaceJsonFormat: RootJsonFormat[TwitterPlace] = jsonFormat6(TwitterPlace)
  implicit val geolocationPlaceJsonFormat: RootJsonFormat[TwitterGeolocation] = jsonFormat2(TwitterGeolocation)
  implicit val tweetJsonFormat: RootJsonFormat[Tweet] = jsonFormat7(Tweet)
  implicit val tweetsCountJsonFormat: RootJsonFormat[TweetCount] = jsonFormat1(TweetCount)
  implicit val twitterUserCountJsonFormat: RootJsonFormat[TwitterUserCount] = jsonFormat1(TwitterUserCount)
  implicit val placeCountJsonFormat: RootJsonFormat[PlaceCount] = jsonFormat1(PlaceCount)
  implicit val geolocationCountJsonFormat: RootJsonFormat[GeolocationCount] = jsonFormat1(GeolocationCount)
}
