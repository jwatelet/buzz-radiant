package be.jwa

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.util.Timeout
import be.jwa.actors.TwitterActor
import be.jwa.actors.TwitterActor.AddTweet
import be.jwa.controllers.Tweet
import be.jwa.services.{BuzzRadiantHttpServices, WebSocketService}
import be.jwa.sources.TwitterSource
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration._


object Boot extends App {
  val logger = LoggerFactory.getLogger(classOf[Config])
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(20.seconds)
  implicit val tweetActor: ActorRef = system.actorOf(TwitterActor.props())
  val listenedHashtags = Seq("#warcraft", "#Blizzard" , "#BFA", "#wow", "#wowclassic")
  val twitterSource: Source[Tweet, NotUsed] = TwitterSource.source(new Config(ConfigFactory.load()), listenedHashtags)

  val graph: RunnableGraph[NotUsed] = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import akka.stream.scaladsl.GraphDSL.Implicits._

    val printlnSink = Sink.foreach(println)
    val flow = Flow[Tweet].map { t =>
      tweetActor ! AddTweet(t)
      t
    }
    twitterSource ~> flow ~> printlnSink

    ClosedShape
  })


  val route = Route.seal(new WebSocketService(twitterSource).route ~ new BuzzRadiantHttpServices().routes)

  graph.run()
  Http().bindAndHandle(route, "localhost", 8080)
  logger.info(s"Server online at http://localhost:8000/")
}
