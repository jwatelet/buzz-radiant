package be.jwa.services

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.jwa.directive.CorsSupport
import be.jwa.services.websocket.WebSocketService

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class BuzzRadiantHttpServices(implicit val buzzObserverActor: ActorRef, implicit val system: ActorSystem,
                              implicit val timeout: Timeout, implicit val ec: ExecutionContext,
                              implicit val materializer: ActorMaterializer) extends TwitterService with TwitterUserService
  with BuzzObserverService with WebSocketService with GeolocationService with PlaceService with CorsSupport {

  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  val mainRoutes: Route = pathEndOrSingleSlash {
    get {
      complete {
        "buzz radiant"
      }
    }
  }

  val routes: Route = {
    Route.seal(
      cors {
        tweetRoutes ~ twitterUserRoutes ~ buzzObserverRoutes ~ geolocationRoutes ~ placeRoutes ~ websocketRoute ~ mainRoutes
      }
    )
  }
}