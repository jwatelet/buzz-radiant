package be.jwa.sources

import java.util.UUID
import java.util.concurrent.{Executors, LinkedBlockingQueue}

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.stream.{Materializer, OverflowStrategy}
import be.jwa.Config
import be.jwa.controllers.{Tweet, TweetExtractor}
import com.google.common.collect.Lists
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.Constants
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.httpclient.auth.OAuth1
import com.twitter.hbc.twitter4j.Twitter4jStatusClient
import com.twitter.hbc.twitter4j.handler.StatusStreamHandler
import com.twitter.hbc.twitter4j.message.{DisconnectMessage, StallWarningMessage}
import twitter4j.{StallWarning, Status, StatusDeletionNotice}

import scala.collection.JavaConverters._
import scala.concurrent.Future


object TwitterSource extends TweetExtractor {
  def source(config: Config, hashtags: Seq[String])(implicit fm: Materializer, system: ActorSystem): Source[Tweet, NotUsed] = {
    import system.dispatcher
    val source: Source[Tweet, ActorRef] = Source.actorRef[Tweet](1000, OverflowStrategy.dropHead)
    val (streamEntry: ActorRef, publisherSource: Source[Tweet, NotUsed]) = source.toMat(BroadcastHub.sink(bufferSize = 1024))(Keep.both).run
    Future(runTwitterClient(config, hashtags, streamEntry))
    publisherSource
  }

  private def runTwitterClient(config: Config, hashtags: Seq[String], streamEntry: ActorRef) {
    val (consumerKey, consumerSecret, token, secret) = config.twitterConfig
    val queue = new LinkedBlockingQueue[String](10000)

    val endpoint = new StatusesFilterEndpoint
    endpoint.trackTerms(hashtags.asJava)

    val auth = new OAuth1(consumerKey, consumerSecret, token, secret)

    val client = new ClientBuilder()
      .name("sampleExampleClient")
      .hosts(Constants.STREAM_HOST)
      .endpoint(endpoint)
      .authentication(auth)
      .processor(new StringDelimitedProcessor(queue))
      .build()
    val numProcessingThreads = 4
    val service = Executors.newFixedThreadPool(numProcessingThreads)

    val t4jClient = new Twitter4jStatusClient(
      client, queue, Lists.newArrayList(listener(streamEntry)), service)

    t4jClient.connect()
    for (_: Int <- 0 to numProcessingThreads) {
      t4jClient.process()
    }
  }

  private def listener(streamEntry: ActorRef) = new StatusStreamHandler() {
    override def onStatus(status: Status) {

      streamEntry ! extractTweet(status)
    }

    override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}

    override def onTrackLimitationNotice(limit: Int): Unit = {}

    override def onScrubGeo(user: Long, upToStatus: Long): Unit = {}

    override def onStallWarning(warning: StallWarning): Unit = {}

    override def onException(e: Exception): Unit = {}

    override def onDisconnectMessage(message: DisconnectMessage): Unit = {}

    override def onStallWarningMessage(warning: StallWarningMessage): Unit = {}

    override def onUnknownMessageType(s: String): Unit = {}
  }
}
