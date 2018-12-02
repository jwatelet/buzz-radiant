package be.jwa.controllers.graph

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import be.jwa.ConfigTwitterCredentials
import be.jwa.sources.{SourceAndTwitterClient, TwitterSource}

trait TwitterSourceMaker {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  def makeTwitterSource(credentials: ConfigTwitterCredentials, hashtags: Seq[String]): SourceAndTwitterClient = {
    TwitterSource.source(credentials, hashtags)
  }
}