package it.ldsoftware.starling.workers

import com.typesafe.config.Config

import scala.jdk.CollectionConverters.ListHasAsScala

package object model {

  /**
    * The data that flows through the process
    */
  type Extracted = Map[String, Any]

  /**
    * The result of an extraction. Contains
    *
    * - a String in Left if some error happened during the process
    * - an Extracted in Right with the real data if the operation was successful
    */
  type ExtractionResult = Either[String, Extracted]

  implicit class ConfigOperations(config: Config) {
    def getConfigSList(path: String): List[Config] = config.getConfigList(path).asScala.toList
  }

}
