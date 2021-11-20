package it.ldsoftware.starling.engine.consumers

import akka.stream.Materializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.starling.engine._
import it.ldsoftware.starling.engine.util.Interpolator._

import scala.concurrent.{ExecutionContext, Future}

class PrinterConsumer(template: String)(implicit val ec: ExecutionContext) extends Consumer with LazyLogging {

  override def consumeSuccess(data: Extracted): Future[ConsumerResult] =
    Future {
      logger.info(template <-- data)
      Consumed("PrinterConsumer")
    }

}

object PrinterConsumer extends ConsumerBuilder {

  override def apply(config: Config, ec: ExecutionContext, mat: Materializer): Consumer =
    new PrinterConsumer(config.getString("template"))(ec)

}
