package com.ibm.weather.analytics.poc.ingest.parquetwriter

import com.ibm.weather.analytics.poc.ingest.parquetwriter.modules.Module
import io.prometheus.client.exporter.HTTPServer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Application extends App with Module {

  val server = new HTTPServer(parquetWriterConfig.prometheusPort)

  Await.result(timeBucketWriter.parquetWriteFlow, Duration.Inf)

}
