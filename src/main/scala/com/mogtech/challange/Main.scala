package com.mogtech.challange

import java.util.{ Properties, Timer, TimerTask }

import com.mogtech.challange.model.StoreNames
import com.mogtech.challange.streams.{ ChallengeStream, ChallengeStreamBuilder }
import com.mogtech.challange.utils.Settings
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.{ KafkaStreams, StoreQueryParameters, StreamsConfig }

object Main extends LazyLogging with App {

  val settings = Settings.conf

  val streamingConfig: Properties = {
    val config = new Properties()
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, settings.bootstrapServer)
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, settings.startingOffset)
    config
  }

  val logConfig =
    Map[String, String]("retention.ms" -> "172800000", "retention.bytes" -> "10000000000", "cleanup.policy" -> "compact,delete")

  // Start global state update streams
  logger.warn(settings.kafkaTopics.logFrameTopicName)
  logger.warn(settings.kafkaTopics.resultsTopicName)

  val challengeStream: ChallengeStream =
    new ChallengeStreamBuilder(streamingConfig, settings.kafkaTopics, logConfig).newStream

  val stream = challengeStream.kafkaStream

  //   fix error where calling state listener on CREATED
  while (stream.state() != KafkaStreams.State.CREATED) {
    while (stream.state() == KafkaStreams.State.RUNNING) {
      val data =
        stream.store(StoreQueryParameters.fromNameAndType(StoreNames.resultsRecordOverTimeStore, QueryableStoreTypes.keyValueStore()))
      val timer = new Timer()
      timer.schedule(new TimerTask {
        override def run(): Unit = {
          val allData = data.all()
          while (allData.hasNext) {
            logger.info(allData.next().value)

          }
        }
      }, 100L)
    }
  }
  stream.setStateListener((newState: KafkaStreams.State, oldState: KafkaStreams.State) => {
    logger.info("KStreams state changed from " + oldState + " to " + newState)
  })

}
