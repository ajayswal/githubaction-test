package com.ibm.weather.analytics.poc.ingest.parquetwriter.modules

import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import akka.stream.ActorMaterializer
import com.ibm.weather.analytics.poc.ingest.parquetwriter.configuration.{COSConfiguration, ParquetWriterConfiguration}
import com.ibm.weather.analytics.poc.ingest.parquetwriter.services.{ExportService, MetricsService, TimeBucketWriter}
import com.ibm.weather.analytics.poc.ingest.parquetwriter.BarMessageSchema
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.avro.Schema
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.parquet.avro.AvroReadSupport
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import com.ibm.weather.analytics.ingest.tool.kafka.conf.KafkaEnvReader

import scala.concurrent.ExecutionContextExecutor

trait Module extends COSConfigurationModule {

  implicit val system: ActorSystem                        = ActorSystem("bar-parquet-writer")
  implicit val mat: ActorMaterializer                     = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val config: Config                              = ConfigFactory.load()
  val parquetWriterConfig: ParquetWriterConfiguration     = config.as[ParquetWriterConfiguration]("parquet-writer")
  private val kafkaConsumerConfig: Config                 = KafkaEnvReader().asConfig().withFallback(config.getConfig("akka.kafka.consumer"))

  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(kafkaConsumerConfig, new StringDeserializer, new StringDeserializer)
      .withGroupId(parquetWriterConfig.timeBucketConsumerGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, parquetWriterConfig.autoOffsetResetConfig)

  val schema: Schema = BarMessageSchema.schema

  val dataConsumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(kafkaConsumerConfig, new StringDeserializer, new StringDeserializer)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val cosConf = config.as[COSConfiguration]("cos")

  val hadoopConf = hadoopConfiguration(cosConf)

  val metricsService = new MetricsService()

  val exportService = new ExportService(dataConsumerSettings, hadoopConf, schema, parquetWriterConfig, cosConf)

  val timeBucketWriter = new TimeBucketWriter(metricsService, consumerSettings, exportService, parquetWriterConfig)

}

trait COSConfigurationModule {

  def hadoopConfiguration(cosConf: COSConfiguration): Configuration = {
    val conf   = new Configuration()
    val prefix = cosConf.prefix
    conf.setBoolean(AvroReadSupport.AVRO_COMPATIBILITY, true)
    conf.set(prefix + ".endpoint", cosConf.endpoint)
    conf.set("fs.cos.impl", cosConf.cosImpl)
    conf.set("fs.stocator.scheme.list", cosConf.schemeList)
    conf.set("fs.stocator.cos.impl", cosConf.clientImpl)
    conf.set("fs.stocator.cos.scheme", cosConf.schema)
    conf.set(prefix + ".iam.api.key", cosConf.apiKey)
    conf.set(prefix + ".iam.service.id", cosConf.serviceId)
    conf.set(prefix + ".access.key", cosConf.accessKey)
    conf.set(prefix + ".secret.key", cosConf.secretKey)
    conf
  }

}
