package be.jwa.services.websocket

import java.util.UUID

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Kill}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.util.Timeout
import be.jwa.actors.BuzzActor.{InitStatisticWebsocket, StopStatisticWebsocket}
import be.jwa.actors.WebSocketUser.{ConnectWsHandle, WsHandleDropped}
import be.jwa.actors.{StatPublisher, WebSocketUser}
import be.jwa.controllers.TwitterStatistics
import be.jwa.json.TwitterJsonSupport
import be.jwa.services.websocket.TweetWebSocketFactory.WsHandler
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object StatisticWebSocketFactory {

  case class WsHandler(streamEntry: ActorRef, flow: Flow[Message, Message, NotUsed])

}

trait StatisticWebSocketFactory extends TwitterJsonSupport {
  implicit val timeout: Timeout
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  val buzzObserverActor: ActorRef
  val system: ActorSystem
  private var statisticWSHandlers: Map[UUID, WsHandler] = Map()
  private var statisticPublisher: Map[UUID, ActorRef] = Map()

  def deleteStatisticWebsocketHandler(wsId: UUID): Unit = {
    statisticWSHandlers.get(wsId).foreach { wsHandler =>

      wsHandler.streamEntry ! Kill
      buzzObserverActor ! StopStatisticWebsocket(wsId)
      statisticWSHandlers -= wsId
    }
  }


   def wsUser(observerId : UUID): Flow[Message, Message, NotUsed] = {
    // Create an actor for every WebSocket connection, this will represent the contact point to reach the user
    statisticPublisher.getOrElse(observerId, createStatPublisher(observerId))
    val wsUser: ActorRef = system.actorOf(WebSocketUser.props())

    // Integration point between Akka Streams and the above actor
    val sink: Sink[Message, NotUsed] =
      Flow[Message]
        .to(Sink.actorRef(wsUser, WsHandleDropped)) // connect to the wsUser Actor

    // Integration point between Akka Streams and above actor
    val source: Source[Message, NotUsed] =
      Source
        .actorRef(bufferSize = 10, overflowStrategy = OverflowStrategy.dropBuffer)
        .map((c: TwitterStatistics) => TextMessage.Strict(c.toJson.toString()))
        .mapMaterializedValue { wsHandle =>
          // the wsHandle is the way to talk back to the user, our wsUser actor needs to know about this to send
          // messages to the WebSocket user
          wsUser ! ConnectWsHandle(wsHandle)
          // don't expose the wsHandle anymore
          NotUsed
        }
        .keepAlive(maxIdle = 10.seconds, () => TextMessage.Strict("Keep-alive message sent to WebSocket recipient"))

    Flow.fromSinkAndSource(sink, source)
  }

  private def createStatPublisher(observerId: UUID): ActorRef = {
    val publisher = system.actorOf(StatPublisher.props())
    buzzObserverActor ! InitStatisticWebsocket(observerId, publisher)

    statisticPublisher = statisticPublisher.updated(observerId, publisher)
    publisher
  }
}