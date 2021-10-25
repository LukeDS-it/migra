package it.ldsoftware.starling.extensions

import com.typesafe.config.Config

import scala.jdk.CollectionConverters.ListHasAsScala

object ConfigExtensions {
  implicit class ConfigOperations(config: Config) {
    def getConfigSList(path: String): List[Config] = config.getConfigList(path).asScala.toList
    def getStringOrNull(path: String): String = if (config.hasPath(path)) config.getString(path) else null
  }
}
