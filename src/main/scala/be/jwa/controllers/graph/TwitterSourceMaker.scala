package be.jwa.controllers.graph

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import be.jwa.ConfigTwitterCredentials
import be.jwa.controllers.Tweet
import be.jwa.sources.TwitterSource

trait TwitterSourceMaker {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  def makeTwitterSource(credentials: ConfigTwitterCredentials, hashtags: Seq[String]): Source[Tweet, NotUsed] = {
    TwitterSource.source(credentials, hashtags)
  }
}