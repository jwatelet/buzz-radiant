package be.jwa.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck, Unsubscribe, UnsubscribeAck}
import akka.event.LoggingReceive
import be.jwa.actors.WebSocketUser._

/**
  * Represents a WebSocket user. One of these actors is created for every user that connects to the server
  * over a WebSocket
  *
  */
class WebSocketUser extends Actor with ActorLogging {
  private val id = UUID.randomUUID()
  private val topic = "stat-topic"
  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe(topic, self)
  }

  override def receive: Receive = LoggingReceive {

    case SubscribeAck(Subscribe(`topic`, None, `self`)) =>
      log.info(s"Subscribed user {} to {}", id, `topic`)

    case UnsubscribeAck(Unsubscribe(`topic`, None, `self`)) =>
      log.info("Un-subscribed user {} from {}", id, `topic`)
      context.stop(self)

    case WsHandleDropped =>
      log.warning("Downstream WebSocket has been disconnected, stopping {}", id)
      mediator ! Unsubscribe(topic, self)

  }
}

object WebSocketUser {

  sealed trait Command

  case object WsHandleDropped extends Command

  def props(): Props = Props(new WebSocketUser())
}
