package be.jwa.services.websocket

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import be.jwa.actors.BuzzActor.InitTweetWebsocket
import be.jwa.services.websocket.TweetWebSocketFactory.WsHandler

import scala.concurrent.duration._

object TweetWebSocketFactory {

  case class WsHandler(streamEntry: ActorRef, flow: Flow[Message, Message, NotUsed])

}

trait TweetWebSocketFactory {

  implicit val materializer: ActorMaterializer
  val buzzObserverActor: ActorRef
  private var tweetWSHandlers: Map[UUID, WsHandler] = Map()

  def getOrCreateTweetWebsocketHandler(wsId: UUID): WsHandler = {
    tweetWSHandlers.getOrElse(wsId, createTweetWebsocketHandler(wsId))
  }

  private def createTweetWebsocketHandler(observerId: UUID): WsHandler = {
    val source: Source[Message, ActorRef] =
      Source.actorRef(bufferSize = 1024, overflowStrategy = OverflowStrategy.dropHead)
        .map((s: String) => TextMessage.Strict(s))
        .keepAlive(maxIdle = 10.seconds, () => TextMessage.Strict("Keep-alive message sent to WebSocket recipient"))

    val (streamEntry: ActorRef, messageSource: Source[Message, NotUsed]) = source.toMat(BroadcastHub.sink(1024))(Keep.both).run

    buzzObserverActor ! InitTweetWebsocket(observerId, streamEntry)

    val flow = Flow.fromSinkAndSource(Sink.ignore, messageSource)

    tweetWSHandlers = tweetWSHandlers.updated(observerId, WsHandler(streamEntry, flow))
    tweetWSHandlers(observerId)
  }
}