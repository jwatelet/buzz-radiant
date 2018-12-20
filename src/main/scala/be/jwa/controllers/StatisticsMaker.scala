package be.jwa.controllers

import java.util.Date

import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

case class TwitterStatistics(observedHashtags: Seq[String], tweetCount: Int, userCount: Int,
                             timeStatistics: Seq[TimeStatistic], hashtagStatistics: Seq[HashtagsStatistics])

case class TimeStatistic(date: String, timeInMillis: Long, tweetCount: Int)

case class HashtagsStatistics(hashtag: String, count: Int)

trait StatisticsMaker {
  val hashtags: Seq[String]
  implicit val ec: ExecutionContext

  def makeStatistics(tweetBuffer: ListBuffer[Tweet], timeWindow: Int): Future[TwitterStatistics] = {

    val eventualTimeStatistics = Future(makeTimeStatistic(tweetBuffer, timeWindow))
    val eventualHashtagsStatistics = Future(hashtagsStatistics(tweetBuffer))
    val eventualTweetCount = Future(tweetBuffer.size)
    val eventualUniqueUserCount = Future(tweetBuffer.map(t => t.user).toSet.size)

    hashtagsStatistics(tweetBuffer)
    for {
      timeStatistics <- eventualTimeStatistics
      hashtagStatistics <- eventualHashtagsStatistics
      tweetCount <- eventualTweetCount
      uniqueUserCount <- eventualUniqueUserCount
    } yield {
      TwitterStatistics(hashtags, tweetCount, uniqueUserCount, timeStatistics, hashtagStatistics)
    }
  }

  private def hashtagsStatistics(tweetBuffer: ListBuffer[Tweet]): Seq[HashtagsStatistics] = {
    tweetBuffer.flatMap(t => t.hashTags)
      .groupBy(identity)
      .map { case (hashtag, list) =>
        HashtagsStatistics(hashtag, list.size)
      }
      .toSeq
      .sortBy(hs => -hs.count)
      .take(10)
  }

  private def makeTimeStatistic(tweetBuffer: ListBuffer[Tweet], timeWindow : Int): Seq[TimeStatistic] = tweetBuffer.groupBy(t => roundTime(t.createdAt, 10))
    .map { case (timeInMillis, buffer) =>

      TimeStatistic(new Date(timeInMillis).toString, timeInMillis, buffer.size)
    }
    .toSeq
    .sortBy(t => -t.timeInMillis)

  private def roundTime(timeInMillis: Long, windowMinutes: Int): Long = {
    val dt = new DateTime(timeInMillis, DateTimeZone.UTC)
    dt.withMinuteOfHour((dt.getMinuteOfHour / windowMinutes) * windowMinutes)
      .minuteOfDay
      .roundFloorCopy
      .toDate
      .getTime
  }
}