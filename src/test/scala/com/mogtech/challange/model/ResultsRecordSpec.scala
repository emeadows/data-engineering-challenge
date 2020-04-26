package com.mogtech.challange.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ResultsRecordSpec extends AnyWordSpecLike with Matchers {

  "ResultsRecord" should {
    "decode a record and extract the data" in {
      val user = "8681693f4710c4c5dc9"
      val logInput: String =
        s"""{"ts":1468244384,"uid":"$user","adipiscing":58,"commodo":"D allows writing large code fragments without redundantly"}"""
      ResultsRecord(logInput) shouldBe ResultsRecord(
        "2016-07-11T13:39:00",
        Set("8681693f4710c4c5dc9"),
        Set("ts", "uid", "adipiscing", "commodo")
      )
    }
    "return an empty record if the timestamp is in an incorrect value" in {
      val user = "8681693f4710c4c5dc9"
      val logInput: String =
        s"""{"ts":"","uid":"$user","adipiscing":58,"commodo":"D allows writing large code fragments without redundantly"}"""
      ResultsRecord(logInput) shouldBe ResultsRecord.empty
    }
    "return an empty record if the data is without a user id" in {
      val logInput: String =
        s"""{"ts":1468244384,"adipiscing":58,"commodo":"D allows writing large code fragments without redundantly"}"""
      ResultsRecord(logInput) shouldBe ResultsRecord.empty
    }
  }

}
