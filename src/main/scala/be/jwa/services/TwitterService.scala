package be.jwa.services


import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import be.jwa.actors.TweetCount
import be.jwa.actors.TwitterActor.{GetTweetCount, GetTweets}
import be.jwa.controllers.Tweet
import be.jwa.json.TwitterJsonSupport

trait TwitterService extends TwitterJsonSupport {

  val twitterActor: ActorRef
  implicit val timeout: Timeout
  lazy val tweetRoutes: Route = pathPrefix("tweets") {
    pathEnd {
      get {
        complete {
          (twitterActor ? GetTweets).mapTo[Seq[Tweet]]
        }
      }
    } ~ pathPrefix("count") {
      pathEnd {
        get {
          complete {
            (twitterActor ? GetTweetCount).mapTo[TweetCount]
          }
        }
      }
    }
  }
}