package it.ldsoftware.starling.workers.extractors

import com.typesafe.config.Config

/**
  * This class acts as a factory for an extractor. If you want your extractors to be
  * taken from the process configuration you will need to extend this trait in a companion
  * object of the extractor you're implementing
  */
trait ExtractorBuilder {

  /**
    * Must return an instance of the extractor created with the parameters specified in the
    * configuration
    * @param config the configuration of the extractor
    * @return an instance of a extractor
    */
  def apply(config: Config): Extractor

}
