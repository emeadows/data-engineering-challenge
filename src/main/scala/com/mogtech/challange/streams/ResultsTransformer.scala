package com.mogtech.challange.streams

import com.mogtech.challange.model.{ ResultsRecordOverTime, StoreNames }
import com.typesafe.scalalogging.Logger
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.{ Transformer, TransformerSupplier }
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory

class ResultsTransformer(storeName: String) extends Transformer[String, ResultsRecordOverTime, KeyValue[String, ResultsRecordOverTime]] {

  val logger: Logger = Logger(LoggerFactory.getLogger("benchmark"))

  val resultsKey: String = "RESULTS"

  var context: ProcessorContext              = _
  var store:   KeyValueStore[String, String] = _

  override def init(context: ProcessorContext): Unit = {
    this.context = context
    store = this.context
      .getStateStore(storeName)
      .asInstanceOf[KeyValueStore[String, String]]
  }

  override def transform(key: String, value: ResultsRecordOverTime): KeyValue[String, ResultsRecordOverTime] = {
    logger.info(s"${this.context.offset()} processed at ${this.context.timestamp()}")
    updateResultsRecord(value)
    KeyValue.pair(resultsKey, value)
  }

  private def updateResultsRecord(value: ResultsRecordOverTime): Unit = store.put(resultsKey, value.asResultsDataString)

  override def close(): Unit = {}
}

object ResultsTransformer {

  val transformerSupplier: TransformerSupplier[String, ResultsRecordOverTime, KeyValue[String, ResultsRecordOverTime]] =
    () => new ResultsTransformer(StoreNames.resultsRecordOverTimeStore)
}
