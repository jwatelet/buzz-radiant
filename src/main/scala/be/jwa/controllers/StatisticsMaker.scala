package be.jwa.controllers

import java.util.Date

import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

case class TwitterStatistics(tweetCount: Int, userCount: Int, timeStatistics: Seq[TimeStatistic])

case class TimeStatistic(date: String, timeInMillis: Long, numberOfTweets: Int)


trait StatisticsMaker {

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
      TwitterStatistics(tweetCount, uniqueUserCount, timeStatistics)
    }
  }

  private def makeTimeStatistic(tweetBuffer: ListBuffer[Tweet]) = tweetBuffer.groupBy(t => roundTime(t.createdAt, 10))
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