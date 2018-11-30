package be.jwa.services

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor

class BuzzRadiantHttpServices(implicit val twitterActor: ActorRef, implicit val system: ActorSystem, implicit val timeout: Timeout) extends TwitterService with TwitterUserService {

  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  val mainRoutes: Route =  pathEndOrSingleSlash {
    get {
      complete{
        "hello world"
      }
    }
  }

  val routes: Route = {
    Route.seal(
      tweetRoutes ~ twitterUserRoutes ~ mainRoutes
    )
  }
}
