package it.ldsoftware.starling.workers.consumers

import com.typesafe.config.Config

/**
  * This class acts as a factory for a consumer. If you want your consumers to be
  * taken from the process configuration you will need to extend this trait in a companion
  * object of the consumer you're implementing
  */
trait ConsumerBuilder {

  /**
    * Must return an instance of the consumer created with the parameters specified in the
    * configuration
    * @param config the configuration of the consumer
    * @return an instance of a consumer
    */
  def apply(config: Config): Consumer

}
