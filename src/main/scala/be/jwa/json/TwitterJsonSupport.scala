package be.jwa.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.jwa.actors.{TweetCount, TwitterUserCount}
import be.jwa.controllers.{Tweet, TwitterUser}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TwitterJsonSupport extends SprayJsonSupport with DefaultJsonProtocol with UUIDJsonFormatter {

  implicit val twitterUserJsonFormat: RootJsonFormat[TwitterUser] = jsonFormat6(TwitterUser)
  implicit val tweetJsonFormat: RootJsonFormat[Tweet] = jsonFormat5(Tweet)
  implicit val tweetsCountJsonFormat: RootJsonFormat[TweetCount] = jsonFormat1(TweetCount)
  implicit val twitterUserCountJsonFormat: RootJsonFormat[TwitterUserCount] = jsonFormat1(TwitterUserCount)
}
