package it.ldsoftware.starling.engine

import com.typesafe.config.Config
import it.ldsoftware.starling.engine.extractors.FailFastExtractor
import it.ldsoftware.starling.extensions.ConfigExtensions.ConfigOperations

import scala.concurrent.{ExecutionContext, Future}

/**
  * An extractor is a data producer. Given some parameters it will extract all data matching
  * given parameters.
  */
trait Extractor {

  val config: Config
  val initialValue: Extracted

  private lazy val extractionMode: ExtractionMode =
    config
      .getOptString("mode")
      .map {
        case "merge" => Merge
        case _       => Replace
      }
      .getOrElse(Replace)

  implicit val ec: ExecutionContext

  /**
    * This function must start the data extraction and create a sequence of
    * [[ExtractionResult]] based on the outcome of the operation
    * @return a [[Future]] containing a sequence of [[ExtractionResult]] that will be passed
    *         along the stream
    */
  def doExtract(): Future[Seq[ExtractionResult]]

  def extract(): Future[Seq[ExtractionResult]] =
    doExtract()
      .map { seq =>
        seq.map {
          case Right(value) =>
            extractionMode match {
              case Merge   => Right(value.concat(initialValue))
              case Replace => Right(value)
            }
          case x => x
        }
      }
      .recover {
        case exc => Seq(Left(exc.toString))
      }

  /**
    * This function must return a new instance of the extractor, using extracted data as
    * additional configuration parameters.
    * It may do so, for example, by interpolating the configuration parameters with the actual
    * content of given data.
    * @param data a single data extracted from the previous operation
    * @return a copy of the current extractor, customized with data coming from upstream
    */
  def toPipedExtractor(data: Extracted): Extractor

  final def piped(result: ExtractionResult): Extractor =
    result match {
      case Left(value)  => new FailFastExtractor(value, config)
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
    * @param pc the context in which a process is executed, see the doc for the class.
    * @return an instance of a extractor
    */
  def apply(config: Config, pc: ProcessContext): Extractor

}

sealed trait ExtractionMode

case object Merge extends ExtractionMode
case object Replace extends ExtractionMode
