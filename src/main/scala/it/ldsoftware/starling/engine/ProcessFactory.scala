package it.ldsoftware.starling.engine

import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.engine.consumers.ConsumerFactory
import it.ldsoftware.starling.engine.extractors.ExtractorFactory

import scala.concurrent.ExecutionContext

class ProcessFactory(parLevel: Int) {

  def generateProcess(descriptor: String, ec: ExecutionContext, mat: Materializer): ProcessStream = {
    val config = ConfigFactory.parseString(descriptor)
    val extractors = ExtractorFactory.getExtractors(config, ec, mat)
    val consumers = ConsumerFactory.getConsumers(config, ec, mat)
    new ProcessStream(extractors, consumers, parLevel)
  }

}
