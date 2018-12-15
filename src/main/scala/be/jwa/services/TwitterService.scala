package be.jwa.services


import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import be.jwa.actors.BuzzActor.SendMessageToTwitterActor
import be.jwa.actors.TweetCount
import be.jwa.actors.TwitterActor.{GetStatistics, GetTweetCount, GetTweets}
import be.jwa.controllers.{Tweet, TwitterStatistics}
import be.jwa.json.TwitterJsonSupport

trait TwitterService extends TwitterJsonSupport {

  val buzzObserverActor: ActorRef
  implicit val timeout: Timeout
  lazy val tweetRoutes: Route = pathPrefix("observers" / JavaUUID / "tweets") { observerId =>
    pathEnd {
      get {
        complete {
          (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetTweets)).mapTo[Option[Seq[Tweet]]]
        }
      }
    } ~ pathPrefix("count") {
      pathEnd {
        get {
          complete {
            (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetTweetCount)).mapTo[Option[TweetCount]]
          }
        }
      }
    } ~
      pathPrefix("statistics") {
        pathEnd {
          get {
            complete {
              (buzzObserverActor ? SendMessageToTwitterActor(observerId, GetStatistics)).mapTo[Option[TwitterStatistics]]
            }
          }
        }
      }
  }
}