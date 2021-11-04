package it.ldsoftware.starling.engine.consumers

import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.starling.engine.util.ReflectionFactory
import it.ldsoftware.starling.engine.{Consumer, ConsumerBuilder}
import it.ldsoftware.starling.extensions.ConfigExtensions._

import scala.concurrent.ExecutionContext

object ConsumerFactory {

  final val ConsumersPath = "consume"
  final val ConsumersTypePath = "type"
  final val ConsumersConfigPath = "config"

  def getConsumers(config: Config, ec: ExecutionContext, mat: Materializer): List[Consumer] =
    config.getConfigSList(ConsumersPath).map { c =>
      val cType = c.getString(ConsumersTypePath)
      val cName = s"it.ldsoftware.starling.engine.consumers.$cType"
      val builder = ReflectionFactory.getBuilder[ConsumerBuilder](cName)
      builder(c.getConfig(ConsumersConfigPath), ec, mat)
    }

}
