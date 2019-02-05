package be.jwa.json

import be.jwa.controllers.{HashtagsStatistics, TimeStatistic, TwitterStatistics}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait StatisticsJsonSupport extends DefaultJsonProtocol with UUIDJsonFormatter {
  implicit val timeStatisticsJsonFormat: RootJsonFormat[TimeStatistic] = jsonFormat3(TimeStatistic)
  implicit val hashtagsStatisticsJsonFormat: RootJsonFormat[HashtagsStatistics] = jsonFormat2(HashtagsStatistics)
  implicit val twitterStatisticsJsonFormat: RootJsonFormat[TwitterStatistics] = jsonFormat4(TwitterStatistics)
}
