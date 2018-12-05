package be.jwa.flows

import akka.NotUsed
import akka.stream.scaladsl.Flow
import be.jwa.controllers.{Tweet, TwitterExtractor}
import twitter4j.Status

object ParserStatus extends TwitterExtractor {

  def parse: Flow[Status, Tweet, NotUsed] = Flow[Status].map(extractTweet)
}
