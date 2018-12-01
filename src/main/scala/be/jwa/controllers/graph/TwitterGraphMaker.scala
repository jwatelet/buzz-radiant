package be.jwa.controllers.graph

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.util.Timeout
import be.jwa.actors.TwitterActor.AddTweet
import be.jwa.controllers.Tweet
import org.slf4j.LoggerFactory

trait TwitterGraphMaker {
  implicit val timeout: Timeout

  private val logger = LoggerFactory.getLogger(getClass.getName)

  def makeTwitterGraph(twitterSource: Source[Tweet, NotUsed], tweetActor: ActorRef): RunnableGraph[NotUsed] = {
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      val flow = Flow[Tweet].map { t =>
        tweetActor ? AddTweet(t)
        logger.info(t.toString)
        t
      }
      twitterSource ~> flow ~> Sink.ignore

      ClosedShape
    })
  }
}
