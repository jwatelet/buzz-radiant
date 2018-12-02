package be.jwa.services

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import be.jwa.directive.CorsSupport

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class BuzzRadiantHttpServices(implicit val buzzObserverActor: ActorRef, implicit val system: ActorSystem,
                              implicit val timeout: Timeout, implicit val ec: ExecutionContext) extends TwitterService with TwitterUserService
  with BuzzObserverService with CorsSupport {

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
        tweetRoutes ~ twitterUserRoutes ~ buzzObserverRoutes ~ mainRoutes
      }
    )
  }
}
