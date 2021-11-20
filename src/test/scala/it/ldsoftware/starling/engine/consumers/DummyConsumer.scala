package it.ldsoftware.starling.engine.consumers

import com.typesafe.config.Config
import it.ldsoftware.starling.engine._

import scala.concurrent.{ExecutionContext, Future}

class DummyConsumer(val parameter: String)(implicit val ec: ExecutionContext) extends Consumer {
  override def consumeSuccess(data: Extracted): Future[ConsumerResult] =
    Future.successful(Consumed("DummyConsumer"))
}

object DummyConsumer extends ConsumerBuilder {
  override def apply(config: Config, pc: ProcessContext): Consumer =
    new DummyConsumer(config.getString("parameter"))(pc.executionContext)
}
