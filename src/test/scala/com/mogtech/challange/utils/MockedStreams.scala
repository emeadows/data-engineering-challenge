package com.mogtech.challange.utils

import java.util.{ Properties, UUID }

import org.apache.kafka.common.serialization.Serde

import scala.util.{ Failure, Success, Try }
import org.apache.kafka.streams._
import org.apache.kafka.streams.scala.StreamsBuilder

import collection.JavaConverters._

object MockedStreams {

  def apply(): Builder = Builder()

  case class Builder(topology:      Option[() => Topology] = None,
                     configuration: Properties = new Properties(),
                     stateStores:   Seq[String] = Seq(),
                     initializer:   Option[TopologyTestDriver => Unit] = None) {

    def config(configuration: Properties): Builder =
      this.copy(configuration = configuration)

    def topology(func: StreamsBuilder => Unit): Builder = {
      val buildTopology = () => {
        val builder = new StreamsBuilder()
        func(builder)
        builder.build()
      }
      this.copy(topology = Some(buildTopology))
    }

    def createInput[K, V](topic: String)(implicit serdeKey: Serde[K], serdeValue: Serde[V]): TestInputTopic[K, V] =
      stream.createInputTopic(topic, serdeKey.serializer, serdeValue.serializer)

    def createOutput[K, V](topic: String)(implicit serdeKey: Serde[K], serdeValue: Serde[V]): TestOutputTopic[K, V] =
      stream.createOutputTopic(topic, serdeKey.deserializer, serdeValue.deserializer)

    def inputS[K, V](topic: TestInputTopic[K, V], records: Seq[(K, V)]): Unit =
      topic.pipeKeyValueList(records.map { case (k, v) => new KeyValue(k, v) }.asJava)

    def outputS[K, V](topic: TestOutputTopic[K, V], size: Int): List[(K, V)] = {
      if (size <= 0) throw new ExpectedOutputIsEmpty

      (0 until size).flatMap { _ =>
        Try(topic.readKeyValue()) match {
          case Success(record) => Some((record.key, record.value))
          case Failure(_)      => None
        }
      }.toList
    }

    // state store is temporarily created in ProcessorTopologyTestDriver
    lazy val stream: TopologyTestDriver = {
      val props = new Properties(configuration)
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, s"mocked-${UUID.randomUUID().toString}")
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      new TopologyTestDriver(topology.getOrElse(throw new NoTopologySpecified)(), props)
    }
  }

  class NoTopologySpecified extends Exception("No topology specified. Call topology() on builder.")

  class NoInputSpecified extends Exception("No input fixtures specified. Call input() method on builder.")

  class ExpectedOutputIsEmpty extends Exception("Output size needs to be greater than 0.")

}
