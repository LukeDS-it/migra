package it.ldsoftware.starling.engine.consumers

import com.typesafe.config.Config
import it.ldsoftware.starling.engine.{Consumer, ConsumerBuilder, ProcessContext, getBuilder}
import it.ldsoftware.starling.extensions.ConfigExtensions._

object ConsumerFactory {

  final val ConsumersPath = "consume"
  final val ConsumersTypePath = "type"
  final val ConsumersConfigPath = "config"

  def getConsumers(config: Config, pc: ProcessContext): List[Consumer] =
    config.getConfigSList(ConsumersPath).map { c =>
      val cType = c.getString(ConsumersTypePath)
      val cName = s"it.ldsoftware.starling.engine.consumers.$cType"
      val builder = getBuilder[ConsumerBuilder](cName)
      builder(c.getConfig(ConsumersConfigPath), pc)
    }

}
