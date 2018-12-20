package be.jwa.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.jwa.actors.{BuzzObserverId, GeolocationCount, PlaceCount}
import be.jwa.controllers._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TwitterJsonSupport extends SprayJsonSupport with DefaultJsonProtocol with UUIDJsonFormatter with StatisticsJsonSupport {

  implicit val twitterUserJsonFormat: RootJsonFormat[TwitterUser] = jsonFormat7(TwitterUser)
  implicit val twitterPlaceJsonFormat: RootJsonFormat[TwitterPlace] = jsonFormat6(TwitterPlace)
  implicit val geolocationPlaceJsonFormat: RootJsonFormat[TwitterGeolocation] = jsonFormat2(TwitterGeolocation)
  implicit val tweetJsonFormat: RootJsonFormat[Tweet] = jsonFormat8(Tweet)
  implicit val placeCountJsonFormat: RootJsonFormat[PlaceCount] = jsonFormat1(PlaceCount)
  implicit val geolocationCountJsonFormat: RootJsonFormat[GeolocationCount] = jsonFormat1(GeolocationCount)
  implicit val buzzObserverIdJsonFormat: RootJsonFormat[BuzzObserverId] = jsonFormat2(BuzzObserverId)
}