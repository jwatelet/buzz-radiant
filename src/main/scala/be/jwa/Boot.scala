package be.jwa

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import be.jwa.displays.PrintlnDisplay
import be.jwa.sources.TwitterSource
import com.typesafe.config.ConfigFactory

object Boot extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val source = TwitterSource.source(Config(ConfigFactory.load()), Seq("#rendsunfilmplusmalade", "#thevoicebe"))


  PrintlnDisplay(source).display()

}
