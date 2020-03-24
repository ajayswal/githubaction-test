package com.ibm.weather.analytics.poc.ingest.parquetwriter.services

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.{Date, UUID}

import akka.Done
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.alpakka.avroparquet.scaladsl.AvroParquetSink
import akka.stream.scaladsl.{Keep, Sink}
import com.ibm.weather.analytics.ingest.models.TimeBucket
import com.ibm.weather.analytics.poc.ingest.parquetwriter.BarMessageSchema
import com.ibm.weather.analytics.poc.ingest.parquetwriter.configuration.{COSConfiguration, ParquetWriterConfiguration}
import com.ibm.weather.analytics.poc.ingest.parquetwriter.models.CommittableTimeBucket
import com.weather.bar.{BarMessage, BarMessageProtocol}
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.parquet.avro.AvroParquetWriter
import spray.json._

import scala.concurrent
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class ExportService(dataConsumerSettings: ConsumerSettings[String, String],
                    conf: Configuration,
                    schema: Schema,
                    parquetWriterConfiguration: ParquetWriterConfiguration,
                    cosConf: COSConfiguration)(
    implicit mat: Materializer,
    ex: ExecutionContext
) {

  private val dateFormat     = new SimpleDateFormat("yyyy-MM-dd")
  private val dateTimeFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")

  def writerFlow(timeBucket: CommittableTimeBucket): Future[CommittableTimeBucket] =
    Consumer
      .plainSource(
        dataConsumerSettings
          .withStopTimeout(Duration.Zero)
          .withGroupId(s"${parquetWriterConfiguration.barAndroidConsumerGroup}-${UUID.randomUUID()}")
          .withClientId(s"multiple-time-bucket-id:${timeBucket.id}:${timeBucket.timeBucket.partition}"),
        Subscriptions.assignmentWithOffset(
          new TopicPartition(parquetWriterConfiguration.dataTopic, timeBucket.timeBucket.partition) -> timeBucket.timeBucket.start
        )
      )
      .take(timeBucket.timeBucket.size)
      .async
      .map(parse)
      .toMat(parquetSink(timeBucket.timeBucket))(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()
      .streamCompletion
      .flatMap { _ =>
        timeBucket.committableOffset.commitScaladsl()
      }
      .map(_ => timeBucket)

  private def parse(record: ConsumerRecord[String, String]): GenericRecord = {
    val message = record.value().parseJson.convertTo[BarMessage](BarMessageProtocol.BarMessageFormat)
    BarMessageSchema.toGenericRecord(message)
  }

  private def parquetSink(timeBucket: TimeBucket): Sink[GenericRecord, Future[Done]] = {
    val writer = AvroParquetWriter
      .builder[GenericRecord](
        new Path(
          cosConf.url + s"cos/raw/ingestDate=${date(timeBucket.id, timeBucket.duration)}/ingestOn=${dateTime(timeBucket.id, timeBucket.duration)}/partition=${timeBucket.partition}.parquet"
        )
      )
      .withConf(conf)
      .withSchema(schema)
      .build()
    AvroParquetSink(writer)
  }

  private def date(id: Long, timeBucketDuration: Long): String =
    dateFormat.format(Date.from(Instant.ofEpochMilli(id * timeBucketDuration)))

  private def dateTime(id: Long, timeBucketDuration: Long): String =
    dateTimeFormat.format(Date.from(Instant.ofEpochMilli(id * timeBucketDuration)))

}
