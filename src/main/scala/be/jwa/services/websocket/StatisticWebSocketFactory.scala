package be.jwa.services.websocket

import java.util.UUID

import akka.NotUsed
import akka.actor.{ActorRef, Kill}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy, ThrottleMode}
import be.jwa.actors.BuzzActor.{InitStatisticWebsocket, StopStatisticWebsocket}
import be.jwa.services.websocket.TweetWebSocketFactory.WsHandler

import scala.concurrent.duration._

object StatisticWebSocketFactory {

  case class WsHandler(streamEntry: ActorRef, flow: Flow[Message, Message, NotUsed])

}

trait StatisticWebSocketFactory {

  implicit val materializer: ActorMaterializer
  val buzzObserverActor: ActorRef
  private var statisticWSHandlers: Map[UUID, WsHandler] = Map()

  def getOrCreateStatisticWebsocketHandler(wsId: UUID): WsHandler = {
    statisticWSHandlers.getOrElse(wsId, createStatisticWebsocketHandler(wsId))
  }

  def deleteStatisticWebsocketHandler(wsId: UUID): Unit = {
    statisticWSHandlers.get(wsId).foreach { wsHandler =>

      wsHandler.streamEntry ! Kill
      buzzObserverActor ! StopStatisticWebsocket(wsId)
      statisticWSHandlers -= wsId
    }
  }

  private def createStatisticWebsocketHandler(observerId: UUID): WsHandler = {

    val source: Source[Message, ActorRef] =
      Source.actorRef(bufferSize = 1024, overflowStrategy = OverflowStrategy.dropBuffer)
        .map((s: String) => TextMessage.Strict(s))
        .keepAlive(maxIdle = 10.seconds, () => TextMessage.Strict("Keep-alive message sent to WebSocket recipient"))


    val (streamEntry: ActorRef, messageSource: Source[Message, NotUsed]) = source.toMat(BroadcastHub.sink(1024))(Keep.both).run

    buzzObserverActor ! InitStatisticWebsocket(observerId, streamEntry)

    val flow = Flow.fromSinkAndSource(Sink.ignore, messageSource)
      .throttle(1, 500.milliseconds, 1, ThrottleMode.Shaping)

    statisticWSHandlers = statisticWSHandlers.updated(observerId, WsHandler(streamEntry, flow))
    statisticWSHandlers(observerId)
  }

}