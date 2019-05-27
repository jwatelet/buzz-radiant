package be.jwa.flows

import akka.NotUsed
import akka.stream.scaladsl.Flow
import be.jwa.controllers.{SentimentAnalyzer, Tweet}

object AddSentiment extends SentimentAnalyzer {

  def addSentiment: Flow[Tweet, Tweet, NotUsed] = Flow[Tweet].map{tweet =>
    val sentimentValue = sentiment(tweet.tweetText)
    tweet.copy(sentiment= Some(sentimentValue))
  }
}
