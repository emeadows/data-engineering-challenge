package com.mogtech.challange.model

import io.circe.Decoder.Result
import io.circe.{ Decoder, HCursor }

case class Timestamp(ts: Long)

object Timestamp {

  implicit val decodeLogData: Decoder[Timestamp] = new Decoder[Timestamp] {

    final def apply(c: HCursor): Result[Timestamp] =
      for {
        ts <- c.downField("ts").as[Long]
      } yield Timestamp(ts)
  }

}
