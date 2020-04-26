package com.mogtech.challange.model

import io.circe.{ Decoder, HCursor }

case class UserId(userId: String)

object UserId {

  implicit val decodeLogData: Decoder[UserId] = new Decoder[UserId] {

    final def apply(c: HCursor): Decoder.Result[UserId] =
      for {
        user <- c.downField("uid").as[String]
      } yield {
        UserId(user)
      }
  }

}
