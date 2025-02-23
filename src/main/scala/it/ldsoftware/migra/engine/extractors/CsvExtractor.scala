package it.ldsoftware.migra.engine.extractors

import com.typesafe.config.Config
import it.ldsoftware.migra.engine.*
import it.ldsoftware.migra.extensions.ConfigExtensions.ConfigOperations

import scala.concurrent.{ExecutionContext, Future}

class CsvExtractor(
    csvContent: String,
    separator: String = ",",
    override val config: Config,
    override val initialValue: Extracted = Map()
)(implicit val ec: ExecutionContext)
    extends Extractor {

  override def doExtract(): Future[Seq[ExtractionResult]] = Future {
    csvContent.split("\n").toList match {
      case header :: data => extractRowsWithHeader(header.split(separator).toList, data)
      case Nil            => throw new IllegalArgumentException("Cannot extract: file is empty")
    }
  }

  private def extractRowsWithHeader(header: List[String], rows: List[String]): Seq[ExtractionResult] =
    for {
      row <- rows
    } yield Right(header.zip(row.split(separator)).toMap)

  override def toPipedExtractor(data: Extracted): Extractor = new CsvExtractor(csvContent, separator, config, data)

  override def summary: String = s"CSV Extractor with content $csvContent and separator [$separator]"

}

object CsvExtractor extends ExtractorBuilder {

  override def apply(config: Config, pc: ProcessContext): Extractor = {
    implicit val ec: ExecutionContext = pc.executionContext

    val csvContent = pc.retrieveFile(config.getString("file"))
    val separator = config.getOptString("separator").getOrElse(",")

    new CsvExtractor(csvContent, separator, config)
  }

}
