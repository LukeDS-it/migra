package it.ldsoftware.starling.workers.extractors

import it.ldsoftware.starling.workers.model.{Extracted, ExtractionResult}

import scala.concurrent.{ExecutionContext, Future}

/**
  * An extractor is a data producer. Given some parameters it will extract all data matching
  * given parameters.
  */
trait Extractor {

  implicit val ec: ExecutionContext = ExecutionContext.global

  /**
    * This function must start the data extraction and create a sequence of
    * [[ExtractionResult]] based on the outcome of the operation
    * @return a [[Future]] containing a sequence of [[ExtractionResult]] that will be passed
    *         along the stream
    */
  def extract(): Future[Seq[ExtractionResult]]

  /**
    * This function must return a new instance of the extractor, using extracted data as
    * additional configuration parameters.
    * It may do so, for example, by interpolating the configuration parameters with the actual
    * content of given data.
    * @param data a single data extracted from the previous operation
    * @return a copy of the current extractor, customized with data coming from upstream
    */
  def toPipedExtractor(data: Extracted): Extractor

  final def piped(result: ExtractionResult): Extractor = result match {
    case Left(value)  => new FailFastExtractor(value)
    case Right(value) => toPipedExtractor(value)
  }

}
