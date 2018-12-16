package be.jwa.controllers

import java.util.Date

import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

case class TwitterStatistics(hashtags: Seq[String], tweetCount: Int, userCount: Int, timeStatistics: Seq[TimeStatistic])

case class TimeStatistic(date: String, timeInMillis: Long, tweetCount: Int)


trait StatisticsMaker {
  val hashtags: Seq[String]
  implicit val ec: ExecutionContext

  def makeStatistics(tweetBuffer: ListBuffer[Tweet]): Future[TwitterStatistics] = {

    val eventualTimeStatistics = Future(makeTimeStatistic(tweetBuffer))
    val eventualTweetCount = Future(tweetBuffer.size)
    val eventualUniqueUserCount = Future(tweetBuffer.map(t => t.user).toSet.size)

    for {
      timeStatistics <- eventualTimeStatistics
      tweetCount <- eventualTweetCount
      uniqueUserCount <- eventualUniqueUserCount
    } yield {
      TwitterStatistics(hashtags, tweetCount, uniqueUserCount, timeStatistics)
    }
  }

  private def makeTimeStatistic(tweetBuffer: ListBuffer[Tweet]): Seq[TimeStatistic] = tweetBuffer.groupBy(t => roundTime(t.createdAt, 10))
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