package be.jwa

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{RunnableGraph, Source}
import akka.util.Timeout
import be.jwa.actors.TweetGraphActor.{MakeGraph, MakeTwitterSource}
import be.jwa.actors.{TweetGraphActor, TwitterActor}
import be.jwa.controllers.Tweet
import be.jwa.services.{BuzzRadiantHttpServices, WebSocketService}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


object Boot extends App {
  private val logger = LoggerFactory.getLogger(getClass.getName)
  private implicit val system: ActorSystem = ActorSystem()
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val ec: ExecutionContext = system.dispatcher
  private implicit val timeout: Timeout = Timeout(20.seconds)
  private implicit val tweetActor: ActorRef = system.actorOf(TwitterActor.props(), "tweetActor")


  private val graphActor = system.actorOf(TweetGraphActor.props(), "graphActor")
  private val credentials = new ConfigTwitterCredentials(ConfigFactory.load())
  private val hashtags = Seq("#warcraft", "#Blizzard", "#BFA", "#wow", "#wowclassic")


  for {
    source <- (graphActor ? MakeTwitterSource(credentials, hashtags)).mapTo[Source[Tweet, NotUsed]]
    graph <- (graphActor ? MakeGraph(source, tweetActor)).mapTo[RunnableGraph[NotUsed]]
  } yield {
    graph.run()
    val allRoutes = Route.seal(new WebSocketService(source).route ~ new BuzzRadiantHttpServices().routes)
    Http().bindAndHandle(allRoutes, Config.interface, Config.port)
    logger.info(s"Server online at http://${Config.interface}:${Config.port}/")
  }
}

object Config {
  val interface: String = Option(System.getenv("INTERFACE")).getOrElse("localhost")
  val port: Int = Option(System.getenv("PORT")).getOrElse("8080").toInt
}