package it.ldsoftware.starling.engine.consumers

import com.typesafe.config.Config
import it.ldsoftware.starling.engine.{Consumed, Consumer, ConsumerBuilder, ConsumerResult, Extracted}

import scala.concurrent.Future

class DummyConsumer(val parameter: String) extends Consumer {
  override def consumeSuccess(data: Extracted): Future[ConsumerResult] =
    Future.successful(Consumed("DummyConsumer"))
}

object DummyConsumer extends ConsumerBuilder {
  override def apply(config: Config): Consumer = new DummyConsumer(config.getString("parameter"))
}
