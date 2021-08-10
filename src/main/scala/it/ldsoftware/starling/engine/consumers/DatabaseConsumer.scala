package it.ldsoftware.starling.engine.consumers

import com.typesafe.config.Config
import it.ldsoftware.starling.engine.{Consumer, ConsumerBuilder, ConsumerResult, Extracted}

import scala.concurrent.Future

class DatabaseConsumer extends Consumer {

  override def consumeSuccess(data: Extracted): Future[ConsumerResult] = ???

}

object DatabaseConsumer extends ConsumerBuilder {
  override def apply(config: Config): DatabaseConsumer = new DatabaseConsumer()
}
