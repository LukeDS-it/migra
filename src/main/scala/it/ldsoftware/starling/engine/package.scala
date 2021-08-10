package it.ldsoftware.starling

import com.typesafe.config.Config

import scala.jdk.CollectionConverters.ListHasAsScala

package object engine {

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


  /**
    * Result of the consume operation
    */
  sealed trait ConsumerResult

  /**
    * Used when the consume operation was successful
    * @param consumer the name of the consumer, for logging purposes
    */
  case class Consumed(consumer: String) extends ConsumerResult

  /**
    * Used when the consume operation was not successful
    * @param consumer the name of the consumer, for logging purposes
    * @param reason the reason why the data was not consumed
    * @param data if available, it contains the extracted data
    * @param err if available, it contains the throwable that made the operation fail
    */
  case class NotConsumed(
                          consumer: String,
                          reason: String,
                          data: Option[Extracted],
                          err: Option[Throwable]
                        ) extends ConsumerResult

  implicit class ConfigOperations(config: Config) {
    def getConfigSList(path: String): List[Config] = config.getConfigList(path).asScala.toList
  }

}

