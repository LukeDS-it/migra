package it.ldsoftware.starling.engine.extractors

import com.typesafe.config.Config
import it.ldsoftware.starling.engine.{Extracted, ExtractionResult, Extractor, ExtractorBuilder}

import scala.concurrent.Future

class DummyExtractor(val parameter: String) extends Extractor {
  override def extract(): Future[Seq[ExtractionResult]] =
    Future.successful(Seq(Right(Map("extracted" -> parameter))))

  override def toPipedExtractor(data: Extracted): Extractor = new DummyExtractor(parameter)
}

object DummyExtractor extends ExtractorBuilder {
  override def apply(config: Config): Extractor = new DummyExtractor(config.getString("parameter"))
}
