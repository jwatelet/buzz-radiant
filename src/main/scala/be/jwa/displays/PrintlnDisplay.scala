package be.jwa.displays

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future

case class PrintlnDisplay[A](dataSource: Source[A, NotUsed])(implicit fm: Materializer, system: ActorSystem) {
  def display(): Future[Done] = dataSource.runWith(Sink.foreach(println))
}
