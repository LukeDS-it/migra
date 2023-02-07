package it.ldsoftware.migra.engine.extractors

import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.migra.engine.{Extracted, ExtractionResult, Extractor, ExtractorBuilder, ProcessContext}

import scala.concurrent.{ExecutionContext, Future}

class FlattenExtractor(property: String, override val config: Config, override val initialValue: Extracted)(implicit
    val ec: ExecutionContext,
    mat: Materializer
) extends Extractor {
  override def doExtract(): Future[Seq[ExtractionResult]] =
    Future(initialValue)
      .map(_.get(property))
      .map {
        case Some(value) => flatMapFromAny(value)
        case None        => Seq(Left(s"No property $property found on $initialValue"))
      }

  private def flatMapFromAny(value: Any): Seq[ExtractionResult] = value match {
    case seq if seq.isInstanceOf[Seq[Extracted]] => seq.asInstanceOf[Seq[Extracted]].map(Right(_))
    case _                                       => Seq(Left(s"${value.getClass.getSimpleName} is not a sequence"))
  }

  override def toPipedExtractor(data: Extracted): Extractor =
    new FlattenExtractor(property, config, data)

  override def summary: String = s"FlattenExtractor with property $property and value $initialValue"
}

object FlattenExtractor extends ExtractorBuilder {
  override def apply(config: Config, pc: ProcessContext): Extractor = {
    val property = config.getString("property")
    implicit val executionContext: ExecutionContext = pc.executionContext
    implicit val materializer: Materializer = pc.materializer

    new FlattenExtractor(property, config, Map())
  }
}
