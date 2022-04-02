package it.ldsoftware.starling.engine

import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.engine.consumers.ConsumerFactory
import it.ldsoftware.starling.engine.extractors.ExtractorFactory
import it.ldsoftware.starling.engine.providers.TokenProviderFactory

class ProcessFactory(parLevel: Int) {

  def generateProcess(descriptor: String, pc: ProcessContext): ProcessStream = {
    val config = ConfigFactory.parseString(descriptor)
    val extractors = ExtractorFactory.getExtractors(config, pc)
    val consumers = ConsumerFactory.getConsumers(config, pc)
    TokenProviderFactory
      .getTokenProviders(config, pc)
      .foreach { p =>
        pc.tokenCaches.put(p.name, p)
      }
    new ProcessStream(extractors, consumers, parLevel)
  }

}
