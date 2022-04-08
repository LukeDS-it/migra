package it.ldsoftware.starling.engine.extractors
import com.typesafe.config.Config
import it.ldsoftware.starling.engine.{Extracted, ExtractionResult, Extractor}

import scala.concurrent.{ExecutionContext, Future}

/**
  * This extractor only returns failures. It is useful when wanting to pipe a failure throughout
  * the process chain
  *
  * @param message message indicating the reason of the failure
  */
class FailFastExtractor(message: String, override val config: Config, override val initialValue: Extracted = Map())(
    implicit val ec: ExecutionContext
) extends Extractor {

  override def doExtract(): Future[Seq[ExtractionResult]] =
    Future(Seq(Left(message)))

  override def toPipedExtractor(data: Extracted): Extractor =
    new FailFastExtractor(message, config)

}
