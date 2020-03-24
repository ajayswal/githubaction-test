package com.ibm.weather.analytics.poc.ingest.parquetwriter

import com.weather.bar.{BarMessage, BarMessageProtocol}
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}
import spray.json._

object BarMessageSchema {

  val schema = new Schema.Parser().parse(
    """
       {
           "type": "record",
           "name": "BarMessage",
           "namespace": "com.ibm.weather.analytics.poc.ingest.parquetwriter",
           "fields": [
               {
                  "name": "record",
                  "type": "string"
               }
           ]

       }
    """
  )

  def toGenericRecord(barMessage: BarMessage): GenericRecord =
    new GenericRecordBuilder(schema)
      .set("record", barMessage.toJson(BarMessageProtocol.BarMessageFormat).toString())
      .build()

}
