package be.jwa.services.websocket

import java.util.UUID

import akka.NotUsed
import akka.actor.{ActorRef, Cancellable, Kill}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.pattern.ask
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.util.Timeout
import be.jwa.actors.BuzzActor.{InitStatisticWebsocket, SendMessageToTwitterActor, StopStatisticWebsocket}
import be.jwa.actors.TwitterActor.GetStatistics
import be.jwa.controllers.TwitterStatistics
import be.jwa.json.TwitterJsonSupport
import be.jwa.services.websocket.TweetWebSocketFactory.WsHandler
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object StatisticWebSocketFactory {

  case class WsHandler(streamEntry: ActorRef, flow: Flow[Message, Message, NotUsed])

}

trait StatisticWebSocketFactory extends TwitterJsonSupport {
  implicit val timeout: Timeout
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
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


    val sourceTick: Source[Future[Unit], Cancellable] = Source.tick(0.milliseconds, 10.seconds, (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetStatistics(10)))
      .mapTo[Option[TwitterStatistics]]
      .map(maybeStatistics => maybeStatistics.foreach(s => s.toJson.toString()))
    )


    val source: Source[Message, ActorRef] =
      Source.actorRef(bufferSize = 1024, overflowStrategy = OverflowStrategy.dropBuffer)
        .map((s: String) => TextMessage.Strict(s))
        .keepAlive(maxIdle = 5.seconds, () => TextMessage.Strict("Keep-alive message sent to WebSocket recipient"))


    val (streamEntry: ActorRef, messageSource: Source[Message, NotUsed]) = source.toMat(BroadcastHub.sink(1024))(Keep.both).run

    buzzObserverActor ! InitStatisticWebsocket(observerId, streamEntry)

    val flow = Flow.fromSinkAndSource(Sink.ignore, messageSource)
      .buffer(1, OverflowStrategy.dropHead)

    statisticWSHandlers = statisticWSHandlers.updated(observerId, WsHandler(streamEntry, flow))
    statisticWSHandlers(observerId)
  }

}