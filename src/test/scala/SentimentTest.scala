import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import be.jwa.controllers.Sentiment.{Negative, Positive, VeryNegative, VeryPositive}
import be.jwa.controllers.{SentimentAnalyzer, Tweet, TwitterUser}
import be.jwa.flows.AddSentiment
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

class SentimentTest() extends TestKit(ActorSystem("sentiments-test")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with SentimentAnalyzer {

  implicit val timeout: Timeout = Timeout(20.seconds)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher


  "A sentiment analyzer " must {

    "recognize a positive sentiment" in {
      assert(sentiment("I'm soo happy to read that ! Great news !!!") == Positive)
    }

    "recognize a very negative sentiment" in {
      assert(sentiment("A terrible loss, so sad !") == VeryNegative)
    }

    "recognize a negative sentiment" in {
      assert(sentiment("This is not the best thing I've seen today") == Negative)
    }

    "recognize a very positive sentiment" in {
      assert(sentiment("I'm so excited, greatest taste ever !  it was excellent, such a delightful moment") == VeryPositive)
    }
  }

  "The AddSentiment Flow" must {
    "add sentiment to a Tweet object" in {
      val user = TwitterUser(0L, "", None, "", 0, 0, None, 0L)
      val tweet = Tweet(0L, 0L, "I'm soo happy to read that ! Great news !!!", Nil, user, None, None, false, None)
      val addSentimentFlow = AddSentiment.addSentiment

      val future = Source.single(tweet).via(addSentimentFlow).runWith(Sink.head)
      val result = Await.result(future, 3.seconds)
      assert(result == tweet.copy(sentiment = Some(Positive)))
    }
  }
}