package it.ldsoftware.starling.engine

import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.workers.consumers.ConsumerFactory
import it.ldsoftware.starling.workers.extractors.ExtractorFactory

class ProcessGenerator(parLevel: Int) {

  def generateProcess(hocon: String): DataProcessor = {
    val config = ConfigFactory.parseString(hocon)
    val extractors = ExtractorFactory.getExtractors(config)
    val consumers = ConsumerFactory.getConsumers(config)
    new DataProcessor(extractors, consumers, parLevel)
  }

}
