package be.jwa.services

import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import be.jwa.actors.BuzzActor.{CreateBuzzObserver, DeleteBuzzObserver, GetAllBuzzObserversIds}
import be.jwa.actors.BuzzObserverId
import be.jwa.json.{TwitterJsonSupport, UUIDJsonFormatter}

import scala.concurrent.ExecutionContext

trait BuzzObserverService extends TwitterJsonSupport with UUIDJsonFormatter {

  val buzzObserverActor: ActorRef
  implicit val timeout: Timeout
  implicit val ec: ExecutionContext

  lazy val buzzObserverRoutes: Route = pathPrefix("observers") {
    pathEnd {
      post {
        entity(as[Seq[String]]) { hashtags =>
          complete {
            (buzzObserverActor ? CreateBuzzObserver(hashtags)).mapTo[UUID].map(id => id.toString)
          }
        }
      } ~
        get {
          complete {
            (buzzObserverActor ? GetAllBuzzObserversIds)
              .mapTo[Seq[BuzzObserverId]]
          }
        }
    } ~ pathPrefix(JavaUUID) { observerId =>
      pathEnd {
        delete {
          complete {
            (buzzObserverActor ? DeleteBuzzObserver(observerId)).mapTo[String]
          }
        }
      }
    }
  }
}