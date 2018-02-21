package be.jwa

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape}
import be.jwa.sources.{Tweet, TwitterSource}
import com.typesafe.config.ConfigFactory


object Boot extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val graph: RunnableGraph[NotUsed] = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import akka.stream.scaladsl.GraphDSL.Implicits._
    val twitterSource: Source[Tweet, NotUsed] = TwitterSource.source(Config(ConfigFactory.load()), Seq("#rendsunfilmplusmalade", "#thevoicebe", "#CHEBAR"))
    val printlnSink = Sink.foreach(println)

    twitterSource ~> printlnSink
    ClosedShape
  })

  graph.run()
}
