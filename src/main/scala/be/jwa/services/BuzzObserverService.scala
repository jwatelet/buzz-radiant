package be.jwa.services

import java.util.UUID

import akka.pattern.ask
import akka.actor.ActorRef
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout

import be.jwa.actors.BuzzActor.CreateBuzzObserver
import be.jwa.json.{TwitterJsonSupport, UUIDJsonFormatter}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

trait BuzzObserverService extends TwitterJsonSupport with UUIDJsonFormatter {

  private val logger = LoggerFactory.getLogger(getClass.getName)
  val buzzObserverActor: ActorRef
  implicit val timeout: Timeout
  implicit val ec: ExecutionContext

  lazy val buzzObserverRoutes: Route = pathPrefix("observers") {
    post {
      entity(as[Seq[String]]) { hashtags =>
        complete {
          logger.info(s" hashtags : $hashtags")
          (buzzObserverActor ? CreateBuzzObserver(hashtags)).mapTo[UUID].map(id => id.toString)
        }
      }
    }
  }
}
