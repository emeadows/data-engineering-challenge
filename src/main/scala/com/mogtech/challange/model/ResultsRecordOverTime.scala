package com.mogtech.challange.model

import io.circe.generic.semiauto._
import io.circe._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.util.{ Success, Try }

case class ResultsRecordOverTime(everyMinute: List[ResultsRecord]) {

  def asResultsDataString: String = {
    import ResultsRecordOverTime._
    def displayRecord(resultsRecord: ResultsRecord): String =
      s"""   Time: ${resultsRecord.timestamp}: Users: ${resultsRecord.users.size} UniqueKeys: ${resultsRecord.uniqueKeys.size}"""
    s"""Output:
       |Every Minute:
       |${everyMinute.map(displayRecord).mkString("\n")}
       |Every Hour:
       |${groupHourly(everyMinute).map(displayRecord).mkString("\n")}
       |Every Day:
       |${groupDaily(everyMinute).map(displayRecord).mkString("\n")}
       |Every Weekly:
       |${groupWeekly(everyMinute).map(displayRecord).mkString("\n")}
       |Every Month:
       |${groupMonthly(everyMinute).map(displayRecord).mkString("\n")}
       |Every Year:
       |${groupYearly(everyMinute).map(displayRecord).mkString("\n")}
       |""".stripMargin
  }

}

object ResultsRecordOverTime extends DateHelper {

  val empty: ResultsRecordOverTime = ResultsRecordOverTime(Nil)

  implicit val resultsRecordOverTimeDecoder: Decoder[ResultsRecordOverTime] = deriveDecoder
  implicit val resultsRecordOverTimeEncoder: Encoder[ResultsRecordOverTime] = deriveEncoder

  def update(resultsRecord: ResultsRecord, resultsRecordOverTime: ResultsRecordOverTime): ResultsRecordOverTime =
    Try(LocalDateTime.parse(resultsRecord.timestamp, DateTimeFormatter.ISO_DATE_TIME)) match {
      case Success(ts) =>
        ResultsRecordOverTime(everyMinute = updateEveryMinute(ts, resultsRecord, resultsRecordOverTime.everyMinute))
      case _ => resultsRecordOverTime
    }

  def updateRecord(key: String, resultsRecord: ResultsRecord, existing: List[ResultsRecord]): ResultsRecord =
    ResultsRecord(
      timestamp  = key,
      users      = resultsRecord.users ++ existing.flatMap(_.users).toSet,
      uniqueKeys = resultsRecord.uniqueKeys ++ existing.flatMap(_.uniqueKeys).toSet
    )

  def getAndUpdate(ts: String, resultsRecord: ResultsRecord, existing: List[ResultsRecord]): List[ResultsRecord] = {
    val (matched, rest) = existing.partition(_.timestamp == ts)
    val record          = updateRecord(ts, resultsRecord, matched)
    record :: rest
  }

  def groupRecord(resultsRecords: List[ResultsRecord]): List[ResultsRecord] =
    resultsRecords
      .groupBy(_.timestamp)
      .map(
        grouped =>
          ResultsRecord(
            timestamp  = grouped._1,
            users      = grouped._2.flatMap(_.users).toSet,
            uniqueKeys = grouped._2.flatMap(_.uniqueKeys).toSet
          )
      )
      .toList

  def updateEveryMinute(ldt: LocalDateTime, resultsRecord: ResultsRecord, existing: List[ResultsRecord]): List[ResultsRecord] =
    getAndUpdate(ldt.isoFormatted.substring(0, 16), resultsRecord, existing)

  def groupHourly(resultsRecord: List[ResultsRecord]): List[ResultsRecord] =
    groupRecord(resultsRecord.map(rr => rr.copy(timestamp = rr.timestamp.substring(0, 13))))

  def groupDaily(resultsRecord: List[ResultsRecord]): List[ResultsRecord] =
    groupRecord(resultsRecord.map(rr => rr.copy(timestamp = rr.timestamp.substring(0, 10))))

  def groupWeekly(resultsRecord: List[ResultsRecord]): List[ResultsRecord] =
    groupRecord(
      resultsRecord
        .flatMap { rr =>
          val timestamp = s"${rr.timestamp}:00"
          Try(LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME)) match {
            case Success(ts) =>
              Some(ResultsRecord(ts.beginningOfTheWeek.isoFormatted.substring(0, 10), rr.users, rr.uniqueKeys))
            case _ => None
          }
        }
    )

  def groupMonthly(resultsRecord: List[ResultsRecord]): List[ResultsRecord] =
    groupRecord(resultsRecord.map(rr => rr.copy(timestamp = rr.timestamp.substring(0, 7))))

  def groupYearly(resultsRecord: List[ResultsRecord]): List[ResultsRecord] =
    groupRecord(resultsRecord.map(rr => rr.copy(timestamp = rr.timestamp.substring(0, 4))))
}
