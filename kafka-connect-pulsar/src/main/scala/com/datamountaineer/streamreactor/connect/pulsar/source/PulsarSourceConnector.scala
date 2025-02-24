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
package com.datamountaineer.streamreactor.connect.pulsar.source

import com.datamountaineer.streamreactor.common.utils.JarManifest

import java.util
import com.datamountaineer.streamreactor.connect.pulsar.config.PulsarSourceConfig
import com.datamountaineer.streamreactor.connect.pulsar.config.PulsarSourceSettings
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.source.SourceConnector

import scala.jdk.CollectionConverters.SeqHasAsJava

class PulsarSourceConnector extends SourceConnector with StrictLogging {
  private val configDef = PulsarSourceConfig.config
  private var configProps: util.Map[String, String] = _
  private val manifest = JarManifest(getClass.getProtectionDomain.getCodeSource.getLocation)

  /**
    * States which SinkTask class to use
    */
  override def taskClass(): Class[_ <: Task] = classOf[PulsarSourceTask]

  /**
    * Set the configuration for each work and determine the split
    *
    * @param maxTasks The max number of task workers be can spawn
    * @return a List of configuration properties per worker
    */
  override def taskConfigs(maxTasks: Int): util.List[util.Map[String, String]] = {
    logger.info(s"Setting task configurations for $maxTasks workers.")
    // call settings here makes sure we don't have an exclusive subscription over more than one worker
    PulsarSourceSettings(PulsarSourceConfig(configProps), maxTasks)
    // distribute all kcqls to all workers and let the Pulsar subscription type handle the routing
    (1 to maxTasks).map(_ => configProps).toList.asJava
  }

  /**
    * Start the sink and set to configuration
    *
    * @param props A map of properties for the connector and worker
    */
  override def start(props: util.Map[String, String]): Unit = {
    logger.info(s"Starting Pulsar source connector.")
    configProps = props
  }

  override def stop(): Unit = {}

  override def config(): ConfigDef = configDef

  override def version(): String = manifest.version()
}
