package it.ldsoftware.starling.engine.extractors

import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.starling.engine.{Extracted, ExtractionResult, Extractor, ExtractorBuilder}

import scala.concurrent.{ExecutionContext, Future}

class DummyExtractor(val parameter: String)(implicit val ec: ExecutionContext) extends Extractor {
  override def extract(): Future[Seq[ExtractionResult]] =
    Future.successful(Seq(Right(Map("extracted" -> parameter))))

  override def toPipedExtractor(data: Extracted): Extractor = new DummyExtractor(parameter)
}

object DummyExtractor extends ExtractorBuilder {
  override def apply(config: Config, ec: ExecutionContext, mat: Materializer): Extractor =
    new DummyExtractor(config.getString("parameter"))(ec)
}
