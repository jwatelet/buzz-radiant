package be.jwa.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import be.jwa.controllers.TwitterStatistics

import scala.concurrent.ExecutionContext


object StatPublisher {

  def props()(implicit ec: ExecutionContext): Props = Props(new StatPublisher())
}

class StatPublisher extends Actor with ActorLogging {
  private val topic = "stat-topic"
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  def receive: PartialFunction[Any, Unit] = {
    case stat: TwitterStatistics =>

      mediator ! Publish(topic, stat)
      log.info("Stat received {} ", stat)
  }
}
