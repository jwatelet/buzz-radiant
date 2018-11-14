package be.jwa

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape}
import be.jwa.services.WebSocketService
import be.jwa.sources.{Tweet, TwitterSource}
import com.typesafe.config.ConfigFactory


object Boot extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val listenedHashtags = Seq("#rendsunfilmplusmalade", "#thevoicebe", "#CHEBAR")
  val twitterSource: Source[Tweet, NotUsed] = TwitterSource.source(Config(ConfigFactory.load()), listenedHashtags)

  val graph: RunnableGraph[NotUsed] = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import akka.stream.scaladsl.GraphDSL.Implicits._

    val printlnSink = Sink.foreach(println)
    twitterSource ~> printlnSink

    ClosedShape
  })
  val route = new WebSocketService(twitterSource).route

  graph.run()
  Http().bindAndHandle(route, "localhost", 8000)
  println(s"Server online at http://localhost:8000/")
}
