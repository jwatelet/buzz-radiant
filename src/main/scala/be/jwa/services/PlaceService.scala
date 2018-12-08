package be.jwa.services

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives.{complete, get, pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import be.jwa.actors.BuzzActor.SendMessageToTwitterActor
import be.jwa.actors.PlaceCount
import be.jwa.actors.TwitterActor.{GetPlaceCount, GetPlaces}
import be.jwa.controllers.TwitterPlace
import be.jwa.json.TwitterJsonSupport

trait PlaceService extends TwitterJsonSupport {
  lazy val placeRoutes: Route = pathPrefix("observers" / JavaUUID / "places") { observerId =>
    pathEnd {
      get {
        complete {
          (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetPlaces)).mapTo[Option[Seq[TwitterPlace]]]
        }
      }
    } ~
      pathPrefix("count") {
        pathEnd {
          get {
            complete {
              (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetPlaceCount)).mapTo[Option[PlaceCount]]
            }
          }
        }
      }
  }
  implicit val timeout: Timeout
  val buzzObserverActor: ActorRef
}
