package com.ibm.weather.analytics.poc.ingest.parquetwriter.configuration

case class ParquetWriterConfiguration(writeParallelism: Int,
                                      timeBucketTopic: String,
                                      dataTopic: String,
                                      prometheusPort: Int,
                                      timeBucketConsumerGroup: String,
                                      barAndroidConsumerGroup: String,
                                      envPrefix: String,
                                      autoOffsetResetConfig: String)
