package be.jwa.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.jwa.sources.Tweet
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TweetJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val tweetJsonFormat: RootJsonFormat[Tweet] = jsonFormat4(Tweet)
}
