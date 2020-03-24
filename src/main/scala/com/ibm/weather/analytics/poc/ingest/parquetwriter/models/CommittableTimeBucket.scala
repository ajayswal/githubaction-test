package com.ibm.weather.analytics.poc.ingest.parquetwriter.models

import akka.kafka.ConsumerMessage.CommittableOffset
import com.ibm.weather.analytics.ingest.models.TimeBucket
import io.prometheus.client.Histogram

case class CommittableTimeBucket(timeBucket: TimeBucket, committableOffset: CommittableOffset, timer: Histogram.Timer) {

  val id: Long = timeBucket.id

}
