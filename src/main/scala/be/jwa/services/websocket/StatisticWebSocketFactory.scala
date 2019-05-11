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
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


trait StatisticWebSocketFactory extends TwitterJsonSupport {
  implicit val timeout: Timeout
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  val buzzObserverActor: ActorRef
  val system: ActorSystem

  private var statisticPublisher: Map[UUID, ActorRef] = Map()

  def deleteStatisticPublisher(wsId: UUID): Unit = {
    statisticPublisher.get(wsId).foreach { publisher =>

      publisher ! Kill
      buzzObserverActor ! StopStatisticWebsocket(wsId)
      statisticPublisher -= wsId
    }
  }


  def wsUser(observerId: UUID): Flow[Message, Message, NotUsed] = {
    statisticPublisher.getOrElse(observerId, createStatPublisher(observerId))
    val wsUser: ActorRef = system.actorOf(WebSocketUser.props())

    val sink: Sink[Message, NotUsed] =
      Flow[Message]
        .to(Sink.actorRef(wsUser, WsHandleDropped))

    val source: Source[Message, NotUsed] =
      Source
        .actorRef(bufferSize = 10, overflowStrategy = OverflowStrategy.dropBuffer)
        .map((c: TwitterStatistics) => TextMessage.Strict(c.toJson.toString()))
        .mapMaterializedValue { wsHandle =>
          wsUser ! ConnectWsHandle(wsHandle)
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