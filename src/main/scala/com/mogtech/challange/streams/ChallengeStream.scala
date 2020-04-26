package com.mogtech.challange.streams

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.{ Properties, UUID }

import com.mogtech.challange.model.{ DateHelper, ResultsRecord, ResultsRecordOverTime, StoreNames }
import com.mogtech.challange.utils.KafkaTopics
import io.circe.syntax._
import io.github.azhur.kafkaserdecirce.CirceSupport
import org.apache.kafka.streams.scala.{ Serdes, StreamsBuilder }
import org.apache.kafka.streams.state.{ KeyValueStore, StoreBuilder, Stores }
import org.apache.kafka.streams.{ KafkaStreams, StreamsConfig }

import scala.collection.JavaConverters._
import scala.util.Try

class ChallengeStreamBuilder(config: Properties, topics: KafkaTopics, logConfig: Map[String, String]) {
  def newStream: ChallengeStream = new ChallengeStream(config, topics, logConfig)
}

class ChallengeStream(config: Properties, topics: KafkaTopics, logConfig: Map[String, String])
    extends BaseStream
    with CirceSupport
    with DateHelper {
  override val streamName: String = s"challenge-stream-${UUID.randomUUID()}"

  val streamingConfig: Properties = {
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "challengeStream")
    config
  }

  override def build(builder: StreamsBuilder): Unit = {
    import org.apache.kafka.streams.scala.ImplicitConversions._
    import org.apache.kafka.streams.scala.Serdes._

    val resultsStore: StoreBuilder[KeyValueStore[String, String]] =
      Stores
        .keyValueStoreBuilder(Stores.persistentKeyValueStore(StoreNames.resultsRecordOverTimeStore), Serdes.String, Serdes.String)
        .withLoggingEnabled(logConfig.asJava)
        .withCachingEnabled()

    builder.addStateStore(resultsStore)

    builder
      .stream[String, String](topics.logFrameTopicName)
      .map((_, v) => {
        val resultsRecord = ResultsRecord(v)
        val newDailyKey: String = Try(LocalDateTime.parse(resultsRecord.timestamp, DateTimeFormatter.ISO_DATE_TIME)).toOption
          .map(_.getMinute.asIsoDateTime)
          .getOrElse("UNKNOWN")
        newDailyKey -> resultsRecord // in reality you would have multiple partitions and so the timestamp on key would be better
      })
      .groupByKey
      .aggregate(ResultsRecordOverTime.empty)((_, v, agg) => ResultsRecordOverTime.update(v, agg))
      .toStream
      .transform(ResultsTransformer.transformerSupplier, StoreNames.resultsRecordOverTimeStore)
      .flatMap((_, v) => v.everyMinute.map(r => r.timestamp -> r.asJson))
      .peek((k, v) => logger.trace(s"New output: $k -> $v"))
      .to(topics.resultsTopicName)
  }

  val kafkaStream: KafkaStreams = start(streamingConfig)

}
