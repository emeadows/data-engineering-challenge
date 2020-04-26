package com.mogtech.challange.streams

import java.util.Properties

import com.mogtech.challange.model.ResultsRecord
import com.mogtech.challange.utils.{ KafkaTopics, MockedStreams }
import io.circe.Json
import io.circe.syntax._
import io.github.azhur.kafkaserdecirce.CirceSupport
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.Serdes
import org.apache.kafka.streams.{ StreamsConfig, TestInputTopic, TestOutputTopic }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.collection.immutable

class ChallengeStreamSpec extends AnyWordSpecLike with Matchers {

  "ChallengeStream" should {
    "take log data and a new record with key as ISO date String and value as useful data in Json String" in new TestScope {
      val timestamp = 1468244384
      val user      = "8681693f4710c4c5dc9"
      val logInput: String =
        s"""{"ts":$timestamp,"uid":"$user","adipiscing":58,"commodo":"D allows writing large code fragments without redundantly"}"""

      mockedStream.inputS(inputTopic, Seq((null, logInput)))
      val eventResult: immutable.Seq[(String, Json)] = resultsOutput(1)

      eventResult should have size 1

      eventResult.head._1 shouldBe "2016-07-11T13:39"
      val expectedResult: Json =
        ResultsRecord("2016-07-11T13:39", Set(user), Set("ts", "uid", "adipiscing", "commodo")).asJson

      eventResult.head._2 shouldBe expectedResult
    }

    "aggregate log data on ISO date String and value as useful data in Json String" in new TestScope {
      val timestamp1 = 1468244384
      val timestamp2 = 1468244384
      val timestamp3 = 1468244400
      val user1      = "user1"
      val user2      = "user2"
      val user3      = "user3"
      val logInput1: String = s"""{"ts":$timestamp1, "uid": "$user1"}"""
      val logInput2: String = s"""{"ts":$timestamp2, "uid": "$user2", "foo":"bar"}"""
      val logInput3: String = s"""{"ts":$timestamp3, "uid": "$user3"}"""

      mockedStream.inputS(inputTopic, Seq((null, logInput1), (null, logInput2), (null, logInput3)))
      val eventResult: immutable.Seq[(String, Json)] = resultsOutput(3)

      eventResult should have size 3

      val firstRecord:  (String, Json) = eventResult.head
      val middleRecord: (String, Json) = eventResult(1)
      val lastRecord:   (String, Json) = eventResult.last
      firstRecord._1 shouldBe "2016-07-11T13:39"

      val firstAggregate: ResultsRecord = firstRecord._2.as[ResultsRecord].right.get
      firstAggregate.users shouldBe Set(user1)
      firstAggregate.uniqueKeys shouldBe Set("ts", "uid")

      middleRecord._1 shouldBe "2016-07-11T13:39"
      val middleAggregate: ResultsRecord = middleRecord._2.as[ResultsRecord].right.get
      middleAggregate.users shouldBe Set(user1, user2)
      middleAggregate.uniqueKeys shouldBe Set("ts", "uid", "foo")

      lastRecord._1 shouldBe "2016-07-11T13:40"
      lastRecord._2.as[ResultsRecord].right.get.users shouldBe Set(user3)
    }

    "discard log if not parsable" in new TestScope {
      val logInput: String =
        s"""{"ts":"timestamp","uid":"user","adipiscing":58,"commodo":"D allows writing large code fragments without redundantly"}"""

      mockedStream.inputS(inputTopic, Seq((null, logInput)))
      val eventResult: immutable.Seq[(String, Json)] = resultsOutput(1)

      eventResult should have size 0
    }
  }

  abstract class TestScope extends CirceSupport {

    implicit val stringSerde: Serde[String] = Serdes.String

    private val inputTopicName:  String = "input"
    private val outputTopicName: String = "output"

    private val config = {
      val props = new Properties()
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      props
    }

    val logConfig: Map[String, String] =
      Map[String, String]("retention.ms" -> "172800000", "retention.bytes" -> "10000000000", "cleanup.policy" -> "compact,delete")

    val testStream: ChallengeStream = new ChallengeStream(config, KafkaTopics(inputTopicName, outputTopicName), logConfig)

    val mockedStream: MockedStreams.Builder = MockedStreams().topology { builder =>
      testStream.build(builder)
    }

    val inputTopic:  TestInputTopic[String, String] = mockedStream.createInput(inputTopicName)
    val outputTopic: TestOutputTopic[String, Json]  = mockedStream.createOutput(outputTopicName)

    implicit lazy val serdeJson: Serde[Json] = toSerde[Json]

    def resultsOutput(size: Int): List[(String, Json)] =
      mockedStream.outputS[String, Json](outputTopic, size)
  }
}
