package it.ldsoftware.starling.engine

import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.engine.consumers.ConsumerFactory
import it.ldsoftware.starling.engine.extractors.ExtractorFactory

class ProcessFactory(parLevel: Int) {

  def generateProcess(descriptor: String, pc: ProcessContext): ProcessStream = {
    val config = ConfigFactory.parseString(descriptor)
    val extractors = ExtractorFactory.getExtractors(config, pc)
    val consumers = ConsumerFactory.getConsumers(config, pc)
    new ProcessStream(extractors, consumers, parLevel)
  }

}
