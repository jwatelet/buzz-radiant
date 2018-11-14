package be.jwa.services

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor

class BuzzRadiantHttpServices(implicit val tweetActor: ActorRef, implicit val system: ActorSystem, implicit val timeout: Timeout) extends TweetService {

  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  val routes: Route = {
    Route.seal(
      tweetRoutes
    )
  }
}
