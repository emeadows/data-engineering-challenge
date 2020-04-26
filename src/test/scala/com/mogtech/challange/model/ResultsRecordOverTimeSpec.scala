package com.mogtech.challange.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ResultsRecordOverTimeSpec extends AnyWordSpecLike with Matchers {

  "update" should {

    "update a record in the correct format for every time period" in {
      ResultsRecordOverTime.update(
        ResultsRecord("2016-07-11T13:39:00", Set("8681693f4710c4c5dc9"), Set("ts", "uid", "adipiscing", "commodo")),
        ResultsRecordOverTime.empty
      ) shouldBe
        ResultsRecordOverTime(
          everyMinute = List(ResultsRecord("2016-07-11T13:39", Set("8681693f4710c4c5dc9"), Set("ts", "uid", "adipiscing", "commodo")))
        )
    }

    "cope with updating records" in {
      ResultsRecordOverTime.update(
        ResultsRecord("2016-07-11T13:41:00", Set("5678"), Set("ts", "uid", "foo", "bar")),
        ResultsRecordOverTime(
          everyMinute = List(
            ResultsRecord("2016-07-11T13:39", Set("8681693f4710c4c5dc9"), Set("ts", "uid", "adipiscing", "commodo")),
            ResultsRecord("2016-07-11T13:41:00", Set("5678"), Set("ts", "uid", "foo", "bar"))
          )
        )
      )
    }

    "display data in timesplits" in {
      ResultsRecordOverTime(
        everyMinute = List(
          ResultsRecord("2016-07-11T13:39", Set("8681693f4710c4c5dc9"), Set("ts", "uid", "adipiscing", "commodo")),
          ResultsRecord("2016-07-11T13:41", Set("5678"), Set("ts", "uid", "foo", "bar"))
        )
      ).asResultsDataString shouldBe "Output:\nEvery Minute:\n   Time: 2016-07-11T13:39: Users: 1 UniqueKeys: 4\n   Time: 2016-07-11T13:41: Users: 1 UniqueKeys: 4\nEvery Hour:\n   Time: 2016-07-11T13: Users: 2 UniqueKeys: 6\nEvery Day:\n   Time: 2016-07-11: Users: 2 UniqueKeys: 6\nEvery Weekly:\n   Time: 2016-07-11: Users: 2 UniqueKeys: 6\nEvery Month:\n   Time: 2016-07: Users: 2 UniqueKeys: 6\nEvery Year:\n   Time: 2016: Users: 2 UniqueKeys: 6\n"
    }

  }
}
