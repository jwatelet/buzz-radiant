package be.jwa.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck, Unsubscribe, UnsubscribeAck}
import akka.event.LoggingReceive
import be.jwa.actors.WebSocketUser._
import be.jwa.controllers.TwitterStatistics

/**
  * Represents a WebSocket user. One of these actors is created for every user that connects to the server
  * over a WebSocket
  *
  */
class WebSocketUser extends Actor with ActorLogging {
  private val id = UUID.randomUUID()
  private val topic = "stat-topic"
  private val mediator = DistributedPubSub(context.system).mediator
  private var wsHandle: Option[ActorRef] = None

  private def messageWsHandle(stat: TwitterStatistics): Unit =
    wsHandle.fold(())(actor => actor ! stat)

  override def preStart(): Unit = {
    mediator ! Subscribe(topic, self)
  }

  override def receive: Receive = LoggingReceive {

    case SubscribeAck(Subscribe(`topic`, None, `self`)) =>
      log.info("Subscribed {} to {}", id, `topic`)

    case UnsubscribeAck(Unsubscribe(`topic`, None, `self`)) =>
      log.info("Un-subscribed {} from {}", id, `topic`)
      context.stop(self)

    case c: Command =>
      c match {
        // `actorRef` is a handle to communicate back to the WebSocket user
        case ConnectWsHandle(actorRef) =>
          wsHandle = Some(actorRef)

        case WsHandleDropped =>
          log.warning("Downstream WebSocket has been disconnected, stopping {}", id)
          mediator ! Unsubscribe(topic, self)
      }

    case s: String =>
      log.info("Got {}", s)

    case stat: TwitterStatistics =>
      messageWsHandle(stat)
      log.debug("Received TwitterStatistics {} to {}", id, `topic`)

    case _ =>
      log.info("unhandled")
  }
}

object WebSocketUser {

  sealed trait Command

  case class ConnectWsHandle(actorRef: ActorRef) extends Command

  case object WsHandleDropped extends Command

  def props(): Props = Props(new WebSocketUser())
}
