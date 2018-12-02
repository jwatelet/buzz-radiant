import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import be.jwa.actors.BuzzActor.{CreateBuzzObserver, GetAllBuzzObservers, SendMessageToTwitterActor}
import be.jwa.actors.TwitterActor.{GetTweetCount, GetTweets, GetUsersCount}
import be.jwa.actors.{BuzzActor, TweetCount, TwitterUserCount}
import be.jwa.controllers.{Tweet, TwitterUser}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class BuzzActorTest() extends TestKit(ActorSystem("buzz-actor-test")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val timeout: Timeout = Timeout(20.seconds)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher


  "An Buzz Actor " must {

    "create a buzz observer" in {

      val buzzActor = system.actorOf(BuzzActor.props())
      buzzActor ! CreateBuzzObserver(Seq("#buzz", "#test"))

      expectMsgType[UUID](5.seconds)
    }
  }

  "get all buzz observers, the right number you have instanciate" in {
    val buzzActor = system.actorOf(BuzzActor.props())

    Await.result(buzzActor ? CreateBuzzObserver(Seq("#buzz", "#test")), 5.seconds)
    Await.result(buzzActor ? CreateBuzzObserver(Seq("#buzz2", "#test2")), 5.seconds)

    Await.result((buzzActor ? GetAllBuzzObservers).mapTo[Map[UUID, ActorRef]], 5.seconds).size shouldEqual 2
  }


  "instanciate a buzz observer and send tweet count message" in {
    val buzzActor = system.actorOf(BuzzActor.props())

    val id: UUID = Await.result((buzzActor ? CreateBuzzObserver(Seq("#buzz", "#test"))).mapTo[UUID], 5.seconds)

    buzzActor ! SendMessageToTwitterActor(id, GetTweetCount)

    expectMsgType[Option[TweetCount]]
  }

  "instanciate a buzz observer and send users count message" in {
    val buzzActor = system.actorOf(BuzzActor.props())

    val id: UUID = Await.result((buzzActor ? CreateBuzzObserver(Seq("#buzz", "#test"))).mapTo[UUID], 5.seconds)

    buzzActor ! SendMessageToTwitterActor(id, GetUsersCount)

    expectMsgType[Option[TwitterUserCount]]
  }

  "instanciate a buzz observer and send get all tweet message" in {
    val buzzActor = system.actorOf(BuzzActor.props())

    val id: UUID = Await.result((buzzActor ? CreateBuzzObserver(Seq("#buzz", "#test"))).mapTo[UUID], 5.seconds)

    buzzActor ! SendMessageToTwitterActor(id, GetTweets)

    expectMsgType[Option[Seq[Tweet]]]
  }

  "instanciate a buzz observer and send get all users message" in {
    val buzzActor = system.actorOf(BuzzActor.props())

    val id: UUID = Await.result((buzzActor ? CreateBuzzObserver(Seq("#buzz", "#test"))).mapTo[UUID], 5.seconds)

    buzzActor ! SendMessageToTwitterActor(id, GetTweets)

    expectMsgType[Option[Seq[TwitterUser]]]
  }
}