package be.jwa

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.jwa.actors.BuzzActor
import be.jwa.services.BuzzRadiantHttpServices
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


object Boot extends App {
  private val logger = LoggerFactory.getLogger(getClass.getName)
  private implicit val system: ActorSystem = ActorSystem()
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val ec: ExecutionContext = system.dispatcher
  private implicit val timeout: Timeout = Timeout(20.seconds)
  private implicit val buzzActor: ActorRef = system.actorOf(BuzzActor.props(), "buzz-actor")


  val allRoutes = Route.seal(new BuzzRadiantHttpServices().routes)
  Http().bindAndHandle(allRoutes, Config.interface, Config.port)
  logger.info(s"Server online at http://${Config.interface}:${Config.port}/")
}

object Config {
  val interface: String = Option(System.getenv("INTERFACE")).getOrElse("localhost")
  val port: Int = Option(System.getenv("PORT")).getOrElse("8080").toInt

}