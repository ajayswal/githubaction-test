package com.ibm.weather.analytics.poc.ingest.parquetwriter.configuration

case class COSConfiguration(url: String,
                            prefix: String,
                            endpoint: String,
                            cosImpl: String,
                            schemeList: String,
                            schema: String,
                            clientImpl: String,
                            apiKey: String,
                            serviceId: String,
                            accessKey: String,
                            secretKey: String)
