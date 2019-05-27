package be.jwa.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import be.jwa.actors.{BuzzObserverId, GeolocationCount, PlaceCount}
import be.jwa.controllers._
import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, RootJsonFormat}
import spray.json._


trait TwitterJsonSupport extends SprayJsonSupport with DefaultJsonProtocol with UUIDJsonFormatter with StatisticsJsonSupport {

  implicit val twitterUserJsonFormat: RootJsonFormat[TwitterUser] = jsonFormat8(TwitterUser)
  implicit val twitterPlaceJsonFormat: RootJsonFormat[TwitterPlace] = jsonFormat6(TwitterPlace)
  implicit val geolocationPlaceJsonFormat: RootJsonFormat[TwitterGeolocation] = jsonFormat2(TwitterGeolocation)
  implicit val tweetJsonFormat: RootJsonFormat[Tweet] = jsonFormat9(Tweet)
  implicit val placeCountJsonFormat: RootJsonFormat[PlaceCount] = jsonFormat1(PlaceCount)
  implicit val geolocationCountJsonFormat: RootJsonFormat[GeolocationCount] = jsonFormat1(GeolocationCount)
  implicit val buzzObserverIdJsonFormat: RootJsonFormat[BuzzObserverId] = jsonFormat2(BuzzObserverId)

implicit object SentimentJsonFormat extends JsonFormat[Sentiment]{
  override def read(json: JsValue): Sentiment ={
    json.asInstanceOf[JsString].value match{
      case "--" => Sentiment.VeryNegative
      case "-" => Sentiment.Negative
      case "0" => Sentiment.Neutral
      case "+" => Sentiment.Positive
      case "++" => Sentiment.VeryPositive
    }
  }

  override def write(obj: Sentiment): JsValue = {
    obj match {
      case Sentiment.VeryNegative => JsString("--")
      case Sentiment.Negative => JsString("-")
      case Sentiment.Neutral => JsString("0")
      case Sentiment.Positive=> JsString("+")
      case Sentiment.VeryPositive => JsString("++")
    }
  }
}
}