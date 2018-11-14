package be.jwa.filters

import akka.NotUsed
import akka.stream.scaladsl.Flow
import be.jwa.sources.Tweet

object FilterPlaces {
  def flow: Flow[Tweet, Tweet, NotUsed] =
    Flow[Tweet].filter(t => t.placeName.isDefined)
}
