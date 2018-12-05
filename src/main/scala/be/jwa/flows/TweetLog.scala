package be.jwa.flows

import akka.NotUsed
import akka.stream.Attributes
import akka.stream.scaladsl.Flow
import be.jwa.controllers.Tweet

object TweetLog {

  def info: Flow[Tweet, Tweet, NotUsed] = Flow[Tweet]
    .log("tweetStream")
    .addAttributes(Attributes.logLevels(
      onElement = Attributes.LogLevels.Info,
      onFailure = Attributes.LogLevels.Error,
      onFinish = Attributes.LogLevels.Info))

}
