package be.jwa.controllers.graph

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.{ClosedShape, KillSwitches, UniqueKillSwitch}
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.util.Timeout
import be.jwa.actors.TwitterActor.AddTweet
import be.jwa.controllers.Tweet
import org.slf4j.LoggerFactory

trait TwitterGraphMaker {
  implicit val timeout: Timeout

  private val logger = LoggerFactory.getLogger(getClass.getName)
  private val switch = KillSwitches.single[Tweet]

  def makeTwitterGraph(twitterSource: Source[Tweet, NotUsed], tweetActor: ActorRef): RunnableGraph[UniqueKillSwitch] = {

    RunnableGraph.fromGraph(GraphDSL.create(switch) { implicit builder =>
      sw =>
        import akka.stream.scaladsl.GraphDSL.Implicits._

        val flow = Flow[Tweet].map { t =>
          tweetActor ? AddTweet(t)
          logger.info(t.toString)
          t
        }
        twitterSource ~> flow ~> sw ~> Sink.ignore

        ClosedShape
    })
  }
}
