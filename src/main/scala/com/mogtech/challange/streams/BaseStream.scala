package com.mogtech.challange.streams

import java.time.Duration
import java.util.Properties

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.{ KafkaStreams, TopologyDescription }

trait BaseStream extends LazyLogging {

  val streamName: String

  protected def build(builder: StreamsBuilder)

  val NoValue: Null = null

  def start(streamingConfig: Properties): KafkaStreams = {

    // Create a new builder
    val builder: StreamsBuilder = new StreamsBuilder

    // Invoke the custom build function
    build(builder)

    val resultBuilder = builder.build()

    // Output topology
    val description: TopologyDescription = resultBuilder.describe()
    logger.info(description.toString)

    // Create a configured stream using the provided stream builder and stream config
    val kafkaStreams: KafkaStreams = new KafkaStreams(resultBuilder, streamingConfig)

    // Set the uncaught exception handler for properly shutting down in case of errors
    kafkaStreams.setUncaughtExceptionHandler(
      (_: Thread, e: Throwable) =>
        try {
          logger.error(s"Stream terminated because of uncaught exception .. Shutting down app: $e")
          e.printStackTrace()
          val closed: Unit = kafkaStreams.close()
          logger.warn(s"Exiting application after streams close ($closed) after exception")
        } catch {
          case x: Exception => x.printStackTrace()
        } finally {
          logger.warn("Exiting application ..")
          System.exit(-1)
        }
    )

    // Set the shutdown hook in order to close down this stream properly
    sys.ShutdownHookThread {
      logger.info("Shutting down user command processor")
      kafkaStreams.close(Duration.ofSeconds(10))
      ()
    }

    // Finally start the stream
    logger.info(s"Starting stream $streamName")
    kafkaStreams.start()
    kafkaStreams
  }
}
