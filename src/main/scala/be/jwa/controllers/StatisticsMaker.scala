package be.jwa.controllers

import java.util.Date

import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.{ExecutionContext, Future}

case class TwitterStatistics(observedHashtags: Seq[String], userCount: Int,
                             timeStatistics: Seq[TimeStatistic], hashtagStatistics: Seq[HashtagsStatistics])

case class TimeStatistic(date: String, timeInMillis: Long, tweetCount: Int)

case class HashtagsStatistics(hashtag: String, count: Int)

trait StatisticsMaker {
  val hashtags: Seq[String]
  implicit val ec: ExecutionContext

  def makeStatistics(timeCount: Map[Long, Int], tweetCount: Int): Future[TwitterStatistics] = {

    val eventualTimeStatistics = Future(makeTimeStatistic(timeCount))
    val eventualTweetCount = Future(tweetCount)

    for {
      timeStatistics <- eventualTimeStatistics
      tweetCount <- eventualTweetCount
    } yield {
      TwitterStatistics(hashtags, tweetCount, timeStatistics, Seq())
    }
  }

  protected def hashtagsStatistics(tweets: List[Tweet]): Seq[HashtagsStatistics] = {
    tweets.flatMap(t => t.hashTags)
      .groupBy(identity)
      .map { case (hashtag, list) =>
        HashtagsStatistics(hashtag, list.size)
      }
      .toSeq
      .sortBy(hs => -hs.count)
      .take(10)
  }

  protected def makeTimeStatistic(timeCount: Map[Long, Int]): Seq[TimeStatistic] = timeCount.map { case (timeInMillis, count) =>
    TimeStatistic(new Date(timeInMillis).toString, timeInMillis, count)
  }
    .toSeq
    .sortBy(t => -t.timeInMillis)

  protected def roundTime(timeInMillis: Long, windowMinutes: Int): Long = {
    val dt = new DateTime(timeInMillis, DateTimeZone.UTC)
    dt.withMinuteOfHour((dt.getMinuteOfHour / windowMinutes) * windowMinutes)
      .minuteOfDay
      .roundFloorCopy
      .toDate
      .getTime
  }

  protected def addCountToTimeMapMap(tweet: Tweet, timeCountMap: Map[Long, Int], timeWindow: Int): (Long, Int) = {

    val roundedTime = roundTime(tweet.createdAt, timeWindow)
    val count = timeCountMap.getOrElse(roundedTime, 0) + 1

    roundedTime -> count
  }
}