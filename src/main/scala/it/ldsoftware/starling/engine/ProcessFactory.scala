package it.ldsoftware.starling.engine

import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.engine.consumers.ConsumerFactory
import it.ldsoftware.starling.engine.extractors.ExtractorFactory

class ProcessFactory(parLevel: Int) {

  def generateProcess(hocon: String): ProcessExecutor = {
    val config = ConfigFactory.parseString(hocon)
    val extractors = ExtractorFactory.getExtractors(config)
    val consumers = ConsumerFactory.getConsumers(config)
    new ProcessExecutor(extractors, consumers, parLevel)
  }

}
