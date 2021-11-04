package it.ldsoftware.starling.engine

import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.starling.engine.extractors.FailFastExtractor

import scala.concurrent.{ExecutionContext, Future}

/**
  * An extractor is a data producer. Given some parameters it will extract all data matching
  * given parameters.
  */
trait Extractor {

  implicit val ec: ExecutionContext

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

/**
  * This class acts as a factory for an extractor. If you want your extractors to be
  * taken from the process configuration you will need to extend this trait in a companion
  * object of the extractor you're implementing
  */
trait ExtractorBuilder {

  /**
    * Must return an instance of the extractor created with the parameters specified in the
    * configuration
    * @param config the configuration of the extractor
    * @param ec the execution context if needed for Future operations
    * @param mat the akka actor system materializer, in case it is needed
    * @return an instance of a extractor
    */
  def apply(config: Config, ec: ExecutionContext, mat: Materializer): Extractor

}
