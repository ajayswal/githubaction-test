package com.ibm.weather.analytics.poc.ingest.parquetwriter.services

import com.ibm.weather.analytics.poc.ingest.parquetwriter.models.CommittableTimeBucket
import io.prometheus.client.{Counter, Gauge, Histogram}

class MetricsService {

  private val tag = "parquet_writer"

  private val exportedParquetFiles = Counter
    .build()
    .name("exported_parquet_files")
    .help("Exported parquet files.")
    .register()

  private val readTimeBuckets = Counter
    .build()
    .name("read_time_buckets_counter")
    .help("Consumed TimeBuckets from Kafka")
    .register()

  private val timeBucketsGauge = Gauge
    .build()
    .name(s"${tag}_time_buckets_in_progress")
    .help("Time buckets in progress")
    .register()

  private val processedTimeBucketMessagesGauge = Gauge
    .build()
    .name(s"${tag}_time_bucket_processed_messages")
    .help("Time buckets in progress")
    .register()

  private val timeBucketLatency: Histogram = Histogram
    .build()
    .name(s"${tag}_time_bucket_latency_seconds")
    .help("Time bucket processing time")
    .register()

  def exportedParquetFile(timeBucket: CommittableTimeBucket): Unit = {
    timeBucket.timer.observeDuration()
    exportedParquetFiles.inc()
    timeBucketsGauge.dec()
    processedTimeBucketMessagesGauge.inc(timeBucket.timeBucket.size)
  }

  def readTimeBucket(): Histogram.Timer = {
    readTimeBuckets.inc()
    timeBucketsGauge.inc()
    timeBucketLatency.startTimer()
  }

}
