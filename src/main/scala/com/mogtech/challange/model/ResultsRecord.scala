package com.mogtech.challange.model

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.parser.parse

case class ResultsRecord(timestamp: String, users: Set[String], uniqueKeys: Set[String])

object ResultsRecord extends DateHelper {

  implicit val resultsRecordDecoder: Decoder[ResultsRecord] = deriveDecoder
  implicit val resultsRecordEncoder: Encoder[ResultsRecord] = deriveEncoder

  val empty: ResultsRecord = new ResultsRecord("UNKNOWN", Set.empty, Set.empty)

  def apply(value: ResultsRecord, aggregated: ResultsRecord): ResultsRecord =
    value.copy(users = value.users ++ aggregated.users, uniqueKeys = value.uniqueKeys ++ aggregated.uniqueKeys)

  def apply(record: String): ResultsRecord =
    (for {
      json       <- parse(record)
      ts         <- json.as[Timestamp]
      user       <- json.as[UserId]
      uniqueKeys <- Right(json.asObject.map(_.keys.toSet).getOrElse(Set.empty))
    } yield {
      ResultsRecord(ts.ts.asIsoDateTime, Set(user.userId), uniqueKeys)
    }).toOption.getOrElse(empty)
}
