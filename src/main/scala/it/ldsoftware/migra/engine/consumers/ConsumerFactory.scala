package it.ldsoftware.migra.engine.consumers

import com.typesafe.config.Config
import it.ldsoftware.migra.engine.{getBuilder, Consumer, ConsumerBuilder, ProcessContext}
import it.ldsoftware.migra.extensions.ConfigExtensions.*

object ConsumerFactory {

  final val ConsumersPath = "consume"
  final val ConsumersTypePath = "type"
  final val ConsumersConfigPath = "config"

  def getConsumers(config: Config, pc: ProcessContext): List[Consumer] =
    config.getConfigSList(ConsumersPath).map { c =>
      val cType = c.getString(ConsumersTypePath)
      val cName = s"it.ldsoftware.migra.engine.consumers.$cType"
      val builder = getBuilder[ConsumerBuilder](cName)
      builder(c.getConfig(ConsumersConfigPath), pc)
    }

}
