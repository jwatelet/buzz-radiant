import java.util.UUID

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import be.jwa.actors.BuzzActor.{CreateBuzzObserver, DeleteBuzzObserver, GetAllBuzzObserversIds, SendMessageToTwitterActor}
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

    val buzzActor = system.actorOf(BuzzActor.props())
    "create a buzz observer" in {
      buzzActor ! CreateBuzzObserver(Seq("#buzz", "#test"))
      expectMsgType[UUID](5.seconds)
    }


    "get all buzz observers, the right number you have instanciate" in {

      Await.result(buzzActor ? CreateBuzzObserver(Seq("#buzz", "#test")), 5.seconds)
      Await.result(buzzActor ? CreateBuzzObserver(Seq("#buzz2", "#test2")), 5.seconds)

      Await.result((buzzActor ? GetAllBuzzObserversIds).mapTo[Set[UUID]], 5.seconds).size shouldEqual 3
    }


    "instanciate a buzz observer and send tweet count message" in {

      val ids: Set[UUID] = Await.result((buzzActor ? GetAllBuzzObserversIds).mapTo[Set[UUID]], 5.seconds)

      ids.foreach(id => buzzActor ! SendMessageToTwitterActor(id, GetTweetCount))

      expectMsgType[Option[TweetCount]]
    }

    "instanciate a buzz observer and send users count message" in {

      val ids: Set[UUID] = Await.result((buzzActor ? GetAllBuzzObserversIds).mapTo[Set[UUID]], 5.seconds)

      ids.foreach(id => buzzActor ! SendMessageToTwitterActor(id, GetUsersCount))

      expectMsgType[Option[TwitterUserCount]]
    }

    "instanciate a buzz observer and send get all tweet message" in {

      val ids: Set[UUID] = Await.result((buzzActor ? GetAllBuzzObserversIds).mapTo[Set[UUID]], 5.seconds)

      ids.foreach(id => buzzActor ! SendMessageToTwitterActor(id, GetTweets))

      expectMsgType[Option[Seq[Tweet]]]
    }

    "instanciate a buzz observer and send get all users message" in {

      val ids: Set[UUID] = Await.result((buzzActor ? GetAllBuzzObserversIds).mapTo[Set[UUID]], 5.seconds)

      ids.foreach(id =>  buzzActor ! SendMessageToTwitterActor(id, GetTweets))

      expectMsgType[Option[Seq[TwitterUser]]]
    }

    "Delete all observers" in {

      val observersIds: Set[UUID] = Await.result((buzzActor ? GetAllBuzzObserversIds).mapTo[Set[UUID]], 5.seconds)

      observersIds.foreach(id => buzzActor ! DeleteBuzzObserver(id))

      Await.result((buzzActor ? GetAllBuzzObserversIds).mapTo[Set[UUID]], 5.seconds).size shouldEqual 0
    }

  }
}