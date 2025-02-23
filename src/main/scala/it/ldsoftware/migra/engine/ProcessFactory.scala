package it.ldsoftware.migra.engine

import com.typesafe.config.{Config, ConfigFactory}
import io.circe.yaml.v12.parser
import it.ldsoftware.migra.engine.consumers.ConsumerFactory
import it.ldsoftware.migra.engine.extractors.ExtractorFactory
import it.ldsoftware.migra.engine.providers.TokenProviderFactory

class ProcessFactory(parLevel: Int) {

  def generateProcess(descriptor: String, pc: ProcessContext): ProcessStream = {
    val config = parseConfig(descriptor)
    val extractors = ExtractorFactory.getExtractors(config, pc)
    val consumers = ConsumerFactory.getConsumers(config, pc)
    TokenProviderFactory
      .getTokenProviders(config, pc)
      .foreach { p =>
        pc.tokenCaches.put(p.name, p)
      }
    new ProcessStream(extractors, consumers, parLevel)
  }

  private[engine] def parseConfig(descriptor: String): Config =
    parser.parse(descriptor) match {
      case Left(_)      => ConfigFactory.parseString(descriptor)
      case Right(value) => ConfigFactory.parseString(value.toString())
    }

}
