package it.ldsoftware.starling.workers.consumers

import com.typesafe.config.Config
import it.ldsoftware.starling.workers.ReflectionFactory
import it.ldsoftware.starling.workers.model._

object ConsumerFactory {

  final val ConsumersPath = "consume"
  final val ConsumersTypePath = "type"
  final val ConsumersConfigPath = "config"

  def getConsumers(config: Config): List[Consumer] =
    config.getConfigSList(ConsumersPath).map { c =>
      val cType = c.getString(ConsumersTypePath)
      val cName = s"it.ldsoftware.starling.workers.consumers.$cType"
      val builder = ReflectionFactory.getBuilder[ConsumerBuilder](cName)
      builder(c.getConfig(ConsumersConfigPath))
    }

}
