package be.jwa.services

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import be.jwa.actors.TwitterActor.{GetUsers, GetUsersCount}
import be.jwa.actors.TwitterUserCount
import be.jwa.controllers.TwitterUser
import be.jwa.json.TwitterJsonSupport

trait TwitterUserService extends TwitterJsonSupport {

  val twitterActor: ActorRef
  implicit val timeout: Timeout

  lazy val twitterUserRoutes: Route = pathPrefix("users") {
    pathEnd {
      get {
        complete {
          (twitterActor ? GetUsers).mapTo[Set[TwitterUser]]
        }
      }
    } ~
      pathPrefix("count") {
        pathEnd {
          get {
            complete {
              (twitterActor ? GetUsersCount).mapTo[TwitterUserCount]
            }
          }
        }
      }
  }
}
