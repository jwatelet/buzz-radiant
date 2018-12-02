package be.jwa.services

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import be.jwa.actors.BuzzActor.SendMessageToTwitterActor
import be.jwa.actors.TwitterActor.{GetUsers, GetUsersCount}
import be.jwa.actors.TwitterUserCount
import be.jwa.controllers.TwitterUser
import be.jwa.json.TwitterJsonSupport

trait TwitterUserService extends TwitterJsonSupport {

  val buzzObserverActor: ActorRef
  implicit val timeout: Timeout

  lazy val twitterUserRoutes: Route = pathPrefix("observers" / JavaUUID / "users") { observerId =>
    pathEnd {
      get {
        complete {
          (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetUsers)).mapTo[Option[Set[TwitterUser]]]
        }
      }
    } ~
      pathPrefix("count") {
        pathEnd {
          get {
            complete {
              (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetUsersCount)).mapTo[Option[TwitterUserCount]]
            }
          }
        }
      }
  }
}
