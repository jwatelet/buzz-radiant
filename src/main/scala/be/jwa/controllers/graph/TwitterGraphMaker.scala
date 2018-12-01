package be.jwa.controllers.graph

import akka.pattern.ask
import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.util.Timeout
import be.jwa.actors.TwitterActor.AddTweet
import be.jwa.controllers.Tweet

trait TwitterGraphMaker {
  implicit val timeout: Timeout

  def makeTwitterGraph(twitterSource: Source[Tweet, NotUsed], tweetActor: ActorRef): RunnableGraph[NotUsed] = {
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      val printlnSink = Sink.foreach(println)
      val flow = Flow[Tweet].map { t =>
        tweetActor ? AddTweet(t)
        t
      }
      twitterSource ~> flow ~> printlnSink

      ClosedShape
    })
  }
}
