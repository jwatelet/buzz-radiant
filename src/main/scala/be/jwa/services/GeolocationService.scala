package be.jwa.services

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives.{complete, get, pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import be.jwa.actors.BuzzActor.SendMessageToTwitterActor
import be.jwa.actors.GeolocationCount
import be.jwa.actors.TwitterActor.{GetGeolocationCount, GetGeolocations}
import be.jwa.controllers.TwitterGeolocation
import be.jwa.json.TwitterJsonSupport

trait GeolocationService extends TwitterJsonSupport {
  implicit val timeout: Timeout
  val buzzObserverActor: ActorRef
  lazy val geolocationRoutes: Route = pathPrefix("observers" / JavaUUID / "geolocations") { observerId =>
    pathEnd {
      get {
        complete {
          (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetGeolocations)).mapTo[Option[Seq[TwitterGeolocation]]]
        }
      }
    } ~
      pathPrefix("count") {
        pathEnd {
          get {
            complete {
              (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetGeolocationCount)).mapTo[Option[GeolocationCount]]
            }
          }
        }
      }
  }
}