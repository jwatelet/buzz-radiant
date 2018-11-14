package be.jwa.services


import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import be.jwa.actors.TweetActor.{GetTweetCount, GetTweets}
import be.jwa.actors.TweetCount
import be.jwa.json.TweetJsonSupport
import be.jwa.sources.Tweet

trait TweetService extends TweetJsonSupport {

  val tweetActor: ActorRef
  implicit val timeout: Timeout
  lazy val tweetRoutes: Route = pathPrefix("tweets") {
    pathEnd {
      get {
        complete {
          (tweetActor ? GetTweets).mapTo[Seq[Tweet]]
        }
      }
    } ~ pathPrefix("count") {
      pathEnd {
        get {
          complete {
            (tweetActor ? GetTweetCount).mapTo[TweetCount]
          }
        }
      }
    }
  }
}