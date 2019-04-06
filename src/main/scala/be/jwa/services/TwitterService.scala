package be.jwa.services


import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.pattern.ask
import akka.util.Timeout
import be.jwa.actors.BuzzActor.SendMessageToTwitterActor
import be.jwa.actors.TwitterActor.{GetStatistics, GetTweets}
import be.jwa.controllers.{Tweet, TwitterStatistics}
import be.jwa.json.TwitterJsonSupport

trait TwitterService extends TwitterJsonSupport {

  val buzzObserverActor: ActorRef
  implicit val timeout: Timeout


  implicit def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case _: ArithmeticException =>
        extractUri { _ =>
          complete(HttpResponse(NotAcceptable, entity = "timeWindow have to be > 0"))
        }
    }

  lazy val tweetRoutes: Route = pathPrefix("observers" / JavaUUID / "tweets") { observerId =>
    pathEnd {
      get {
        complete {
          (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetTweets)).mapTo[Option[Seq[Tweet]]]
        }
      }
    } ~
      pathPrefix("statistics") {
        pathEnd {
          get {
            parameters('timeWindow ? 10) { timeWindow =>
              complete {
                (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetStatistics(timeWindow))).mapTo[Option[TwitterStatistics]]
              }
            }

          }
        }
      }
  }
}