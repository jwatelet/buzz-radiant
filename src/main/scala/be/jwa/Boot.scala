package be.jwa

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.util.Timeout
import be.jwa.actors.TweetActor
import be.jwa.actors.TweetActor.AddTweet
import be.jwa.services.{BuzzRadiantHttpServices, WebSocketService}
import be.jwa.sources.{Tweet, TwitterSource}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._


object Boot extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(20.seconds)
  implicit val tweetActor: ActorRef = system.actorOf(TweetActor.props())
  val listenedHashtags = Seq("#WOW", "#Warcraft")
  val twitterSource: Source[Tweet, NotUsed] = TwitterSource.source(Config(ConfigFactory.load()), listenedHashtags)

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
  Http().bindAndHandle(route, "localhost", 8000)
  println(s"Server online at http://localhost:8000/")
}
