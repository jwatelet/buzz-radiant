package be.jwa.services.websocket

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import be.jwa.directive.CorsSupport
import be.jwa.json.TwitterJsonSupport

import scala.concurrent.ExecutionContext

trait WebSocketService extends Directives with TwitterJsonSupport with CorsSupport with TweetWebSocketFactory with StatisticWebSocketFactory {
  implicit val timeout: Timeout
  implicit val system: ActorSystem
  implicit val ec: ExecutionContext
  val buzzObserverActor: ActorRef

  lazy val websocketRoutes: Route = path("observers" / JavaUUID / "tweets" / "ws") { observerId =>
    handleWebSocketMessages(getOrCreateTweetWebsocketHandler(observerId).flow)
  } ~
    path("observers" / JavaUUID / "statistics" / "ws") { observerId =>
      get {
        handleWebSocketMessages(wsUser(observerId))
      } ~ delete {
        complete {
          deleteStatisticPublisher(observerId)
          s" statistic ws for $observerId stopped"
        }
      }
    }
}