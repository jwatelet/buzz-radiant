package be.jwa.flows

import akka.NotUsed
import akka.stream.scaladsl.Flow
import be.jwa.controllers.Tweet

object FilterPlaces {
  def flow: Flow[Tweet, Tweet, NotUsed] =
    Flow[Tweet].filter(t => t.place.isDefined)
}
