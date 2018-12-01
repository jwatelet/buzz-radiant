import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.jwa.actors.BuzzActor.CreateBuzzObserver
import be.jwa.actors.{BuzzActor, TwitterActor}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Test extends App {

  private val logger = LoggerFactory.getLogger(getClass.getName)
  private implicit val system: ActorSystem = ActorSystem()
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val ec: ExecutionContext = system.dispatcher
  private implicit val timeout: Timeout = Timeout(20.seconds)
  private implicit val tweetActor: ActorRef = system.actorOf(TwitterActor.props(), "tweetActor")


  private val graphActor = system.actorOf(BuzzActor.props(), "graphActor")

  (graphActor ? CreateBuzzObserver(Seq("#buzz", "#test"))).mapTo[UUID].map(println)

}
