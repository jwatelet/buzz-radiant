package be.jwa.flows

import akka.NotUsed
import akka.stream.scaladsl.Flow
import be.jwa.controllers.{SentimentAnalyzer, Tweet}

object AddSentiment extends SentimentAnalyzer {

  def addSentiment: Flow[Tweet, Tweet, NotUsed] = Flow[Tweet].map { tweet =>
    val sentimentValue = sentiment(cleanTweetText(tweet.tweetText))
    tweet.copy(sentiment = Some(sentimentValue))
  }


  private def cleanTweetText(tweetText: String) = {
    tweetText.replaceAll("(\\b\\w*RT)|[^a-zA-Z0-9\\s\\.\\,\\!,\\@]", "")
      .replaceAll("(http\\S+)", "")
      .replaceAll("(@\\w+)", "")
  }
}
