package it.ldsoftware.starling.engine.extractors
import it.ldsoftware.starling.engine.{Extracted, ExtractionResult, Extractor}

import scala.concurrent.{ExecutionContext, Future}

/**
  * This extractor only returns failures. It is useful when wanting to pipe a failure throughout
  * the process chain
  *
  * @param message message indicating the reason of the failure
  */
class FailFastExtractor(message: String)(implicit val ec: ExecutionContext) extends Extractor {

  override def extract(): Future[Seq[ExtractionResult]] =
    Future(Seq(Left(message)))

  override def toPipedExtractor(data: Extracted): Extractor =
    new FailFastExtractor(message)

}
