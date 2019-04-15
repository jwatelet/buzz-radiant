package be.jwa.sources

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.stream.{Materializer, OverflowStrategy}
import be.jwa.ConfigTwitterCredentials
import be.jwa.controllers.TwitterExtractor
import twitter4j._
import twitter4j.conf.ConfigurationBuilder

import scala.concurrent.Future

case class SourceAndTwitterClient(source: Source[Status, NotUsed], eventualTwitterClient: Future[TwitterStream])

object TwitterSource extends TwitterExtractor {

  def source(config: ConfigTwitterCredentials, hashtags: Seq[String])(implicit fm: Materializer, system: ActorSystem): SourceAndTwitterClient = {
    import system.dispatcher
    val source: Source[Status, ActorRef] = Source.actorRef[Status](1000, OverflowStrategy.dropHead)
    val (streamEntry: ActorRef, publisherSource: Source[Status, NotUsed]) = source.toMat(BroadcastHub.sink(bufferSize = 1024))(Keep.both).run

    SourceAndTwitterClient(publisherSource, Future(runTwitterClient(config, hashtags, streamEntry)))
  }

  private def runTwitterClient(config: ConfigTwitterCredentials, hashtags: Seq[String], streamEntry: ActorRef): TwitterStream = {
    val (consumerKey, consumerSecret, token, secret) = config.twitterConfig

    val cb = new ConfigurationBuilder().setDebugEnabled(true)
      .setOAuthConsumerKey(consumerKey)
      .setOAuthConsumerSecret(consumerSecret)
      .setOAuthAccessToken(token)
      .setOAuthAccessTokenSecret(secret)

    val twitterStream: TwitterStream = new TwitterStreamFactory(cb.build())
      .getInstance()

    twitterStream.addListener(listener(streamEntry))


    val query = new FilterQuery(hashtags.mkString(" "))
    twitterStream.filter(query)
  }

  private def listener(streamEntry: ActorRef) = new StatusListener() {
    override def onStatus(status: Status) {

      streamEntry ! status
    }

    override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}

    override def onTrackLimitationNotice(limit: Int): Unit = {}

    override def onScrubGeo(user: Long, upToStatus: Long): Unit = {}

    override def onStallWarning(warning: StallWarning): Unit = {}

    override def onException(e: Exception): Unit = {}

  }
}