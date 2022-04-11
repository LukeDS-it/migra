package it.ldsoftware.starling.extensions

import com.typesafe.config.Config

import java.time.Duration
import scala.jdk.CollectionConverters.ListHasAsScala

object ConfigExtensions {
  implicit class ConfigOperations(config: Config) {

    def getConfigSList(path: String): List[Config] =
      if (config.hasPath(path)) config.getConfigList(path).asScala.toList
      else Nil

    def getStringOrNull(path: String): String =
      if (config.hasPath(path)) config.getString(path) else null

    def getOptString(path: String): Option[String] =
      Option(getStringOrNull(path))

    def getOptDuration(path: String): Option[Duration] =
      if (config.hasPath(path)) Some(config.getDuration(path)) else None
  }
}
