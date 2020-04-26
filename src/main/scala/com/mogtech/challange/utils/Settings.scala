package com.mogtech.challange.utils

import com.typesafe.config.ConfigFactory
import pureconfig.ConfigSource

case class Settings(bootstrapServer: String, startingOffset: String, kafkaTopics: KafkaTopics)

case class KafkaTopics(logFrameTopicName: String, resultsTopicName: String)

object Settings {

  import pureconfig.generic.auto._
  val conf: Settings = ConfigSource.fromConfig(ConfigFactory.load()).loadOrThrow[Settings]

}
