package be.jwa.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.jwa.actors.{TweetCount, TwitterUserCount}
import be.jwa.controllers.{Tweet, TwitterGeolocation, TwitterPlace, TwitterUser}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TwitterJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val twitterUserJsonFormat: RootJsonFormat[TwitterUser] = jsonFormat7(TwitterUser)
  implicit val twitterPlaceJsonFormat: RootJsonFormat[TwitterPlace] = jsonFormat6(TwitterPlace)
  implicit val geolocationPlaceJsonFormat: RootJsonFormat[TwitterGeolocation] = jsonFormat2(TwitterGeolocation)
  implicit val tweetJsonFormat: RootJsonFormat[Tweet] = jsonFormat6(Tweet)
  implicit val tweetsCountJsonFormat: RootJsonFormat[TweetCount] = jsonFormat1(TweetCount)
  implicit val twitterUserCountJsonFormat: RootJsonFormat[TwitterUserCount] = jsonFormat1(TwitterUserCount)
}
