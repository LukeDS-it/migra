package it.ldsoftware.starling.engine.extractors

import com.typesafe.config.Config
import it.ldsoftware.starling.engine._

import scala.concurrent.{ExecutionContext, Future}

class DummyExtractor(val parameter: String, override val config: Config, override val initialValue: Extracted = Map())(
    implicit val ec: ExecutionContext
) extends Extractor {

  override def doExtract(): Future[Seq[ExtractionResult]] =
    Future.successful(Seq(Right(Map("extracted" -> parameter))))

  override def toPipedExtractor(data: Extracted): Extractor = new DummyExtractor(parameter, config, data)

  override def summary: String = "Dummy extractor failed"
}

object DummyExtractor extends ExtractorBuilder {
  override def apply(config: Config, pc: ProcessContext): Extractor =
    new DummyExtractor(config.getString("parameter"), config)(pc.executionContext)
}
