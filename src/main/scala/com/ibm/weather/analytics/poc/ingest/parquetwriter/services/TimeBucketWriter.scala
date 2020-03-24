package com.ibm.weather.analytics.poc.ingest.parquetwriter.services

import akka.Done
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import com.ibm.weather.analytics.ingest.models.TimeBucket
import com.ibm.weather.analytics.poc.ingest.parquetwriter.configuration.ParquetWriterConfiguration
import com.ibm.weather.analytics.poc.ingest.parquetwriter.models.CommittableTimeBucket
import io.circe.generic.auto._
import io.circe.parser._
import io.prometheus.client.Histogram

import scala.concurrent.{ExecutionContext, Future}

class TimeBucketWriter(metricsService: MetricsService,
                       consumerSettings: ConsumerSettings[String, String],
                       exportService: ExportService,
                       parquetWriterConf: ParquetWriterConfiguration)(
    implicit mat: Materializer,
    ex: ExecutionContext
) {

  val parquetWriteFlow: Future[Done] = Consumer
    .committableSource(
      consumerSettings,
      Subscriptions.topics(parquetWriterConf.timeBucketTopic)
    )
    .log("consumed-time-buckets")
    .map { record =>
      val timer: Histogram.Timer = metricsService.readTimeBucket()
      parse(record.record.value()).right
        .flatMap(_.as[TimeBucket])
        .right
        .map(CommittableTimeBucket(_, record.committableOffset, timer))
    }
    .log("consumed-result")
    .collect {
      case Right(timeBucket) => timeBucket
    }
    .mapAsync(parquetWriterConf.writeParallelism)(exportService.writerFlow)
    .log("exported-time-bucket")
    .runForeach(metricsService.exportedParquetFile)

}
