/*
 * Copyright 2017-2023 Lenses.io Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.lenses.streamreactor.connect.aws.s3.formats.reader.converters

import io.lenses.streamreactor.connect.aws.s3.config.ReaderBuilderContext
import io.lenses.streamreactor.connect.aws.s3.formats.reader.Converter
import io.lenses.streamreactor.connect.aws.s3.model.Topic
import io.lenses.streamreactor.connect.aws.s3.model.location.S3Location
import io.lenses.streamreactor.connect.aws.s3.source.SourceWatermark
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.source.SourceRecord

import java.time.Instant

class TextConverter(
  watermarkPartition: java.util.Map[String, String],
  topic:              Topic,
  partition:          Integer,
  s3Location:         S3Location,
  lastModified:       Instant,
) extends Converter[String] {
  override def convert(value: String, index: Long): SourceRecord =
    new SourceRecord(
      watermarkPartition,
      SourceWatermark.offset(s3Location, index, lastModified),
      topic.value,
      partition,
      null,
      null,
      Schema.STRING_SCHEMA,
      value,
    )
}

object TextConverter {

  def apply(input: ReaderBuilderContext): TextConverter =
    new TextConverter(input.watermarkPartition,
                      input.targetTopic,
                      input.targetPartition,
                      input.bucketAndPath,
                      input.metadata.lastModified,
    )
}
