package it.ldsoftware.starling.engine.consumers

import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.starling.engine.{Consumed, Consumer, ConsumerBuilder, ConsumerResult, Extracted}

import scala.concurrent.{ExecutionContext, Future}

class DummyConsumer(val parameter: String)(implicit val ec: ExecutionContext) extends Consumer {
  override def consumeSuccess(data: Extracted): Future[ConsumerResult] =
    Future.successful(Consumed("DummyConsumer"))
}

object DummyConsumer extends ConsumerBuilder {
  override def apply(config: Config, ec: ExecutionContext, mat: Materializer): Consumer = new DummyConsumer(config.getString("parameter"))(ec)
}
