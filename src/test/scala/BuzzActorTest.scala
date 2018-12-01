import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import akka.pattern.ask
import be.jwa.actors.BuzzActor.CreateBuzzObserver
import be.jwa.actors.{BuzzActor, TweetGraphActor}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class BuzzActorTest() extends TestKit(ActorSystem("buzz-actor-test")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val timeout: Timeout = Timeout(20.seconds)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  private val buzzActor = system.actorOf(BuzzActor.props(), "graphActor")

  "An Buzz Actor " must {

    "create a buzz observer" in {

      buzzActor ! CreateBuzzObserver(Seq("#buzz", "#test"))

      expectMsgType[UUID](5.seconds)
    }
  }
}
