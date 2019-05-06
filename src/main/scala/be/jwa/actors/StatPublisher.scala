package be.jwa.actors

import akka.actor.{Actor, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import be.jwa.controllers.TwitterStatistics


class StatPublisher extends Actor {
  import akka.cluster.pubsub.DistributedPubSubMediator.Publish

  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  def receive: PartialFunction[Any, Unit] = {
    case stat: TwitterStatistics =>


      mediator ! Publish("stat-topic", stat)
  }
}
