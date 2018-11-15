package be.jwa.services

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import be.jwa.controllers.Tweet
import be.jwa.json.TwitterJsonSupport
import spray.json._

class WebSocketService(dataSource: Source[Tweet, NotUsed])(implicit fm: Materializer, system: ActorSystem) extends Directives with TwitterJsonSupport {

  lazy val route: Route = path("tweets") {
    handleWebSocketMessages(webSocketFlow)
  }

  private def webSocketFlow: Flow[Message, Message, NotUsed] =
    Flow[Message]
      .collect { case TextMessage.Strict(msg) => msg }
      .via(logicFlow)
      .map { msg: String => TextMessage.Strict(msg) }

  private def logicFlow: Flow[String, String, NotUsed] = Flow.fromSinkAndSource(Sink.ignore, dataSource).map(t => t.toJson.toString())
}