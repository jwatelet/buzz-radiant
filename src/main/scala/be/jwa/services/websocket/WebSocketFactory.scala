package be.jwa.services.websocket

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import be.jwa.actors.BuzzActor.InitWebsocket
import be.jwa.services.websocket.WebSocketFactory.WsHandler

import scala.concurrent.duration._

object WebSocketFactory {

  case class WsHandler(streamEntry: ActorRef, flow: Flow[Message, Message, NotUsed])

}


trait WebSocketFactory {

  implicit val materializer: ActorMaterializer
  val buzzObserverActor: ActorRef
  private var wsHandlers: Map[UUID, WsHandler] = Map()

  def getOrCreateWebsocketHandler(wsId: UUID): WsHandler = {
    wsHandlers.getOrElse(wsId, createWebsocketHandler(wsId))
  }

  private def createWebsocketHandler(observerId: UUID): WsHandler = {
    val source: Source[Message, ActorRef] =
      Source.actorRef(bufferSize = 1024, overflowStrategy = OverflowStrategy.dropHead)
        .map((s: String) => TextMessage.Strict(s))
        .keepAlive(maxIdle = 10.seconds, () => TextMessage.Strict("Keep-alive message sent to WebSocket recipient"))

    val (streamEntry: ActorRef, messageSource: Source[Message, NotUsed]) = source.toMat(BroadcastHub.sink(1024))(Keep.both).run

    buzzObserverActor ! InitWebsocket(observerId, streamEntry)

    val flow = Flow.fromSinkAndSource(Sink.ignore, messageSource)

    wsHandlers = wsHandlers.updated(observerId, WsHandler(streamEntry, flow))
    wsHandlers(observerId)
  }
}