package it.ldsoftware.migra.engine

import com.typesafe.config.Config
import it.ldsoftware.migra.engine.extractors.FailFastExtractor
import it.ldsoftware.migra.extensions.ConfigExtensions.ConfigOperations

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
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

  private lazy val conflictResolution: ConflictResolution =
    config.getOptConfig("conflict") match {
      case Some(c) =>
        c.getString("action") match {
          case "prepend" => Prepend(c.getString("value"))
          case "append"  => Append(c.getString("value"))
          case _         => Substitute
        }
      case None => Substitute
    }

  implicit val ec: ExecutionContext

  /**
    * This function must start the data extraction and create a sequence of
    * [[ExtractionResult]] based on the outcome of the operation
    * @return a [[Future]] containing a sequence of [[ExtractionResult]] that will be passed
    *         along the stream
    */
  def doExtract(): Future[Seq[ExtractionResult]]

  /**
    * This function must return a new instance of the extractor, using extracted data as
    * additional configuration parameters.
    * It may do so, for example, by interpolating the configuration parameters with the actual
    * content of given data.
    * @param data a single data extracted from the previous operation
    * @return a copy of the current extractor, customized with data coming from upstream
    */
  def toPipedExtractor(data: Extracted): Extractor

  /**
    * This function must return the summary of the process. It is used by the basic extractor
    * function to create process error information
    * @return a string with the summary of the operations
    */
  def summary: String

  final def extract(): Future[Seq[ExtractionResult]] =
    doExtract()
      .map { seq =>
        seq.map {
          case Right(value) =>
            extractionMode match {
              case Merge   => Right(concatValues(initialValue, value))
              case Replace => Right(value)
            }
          case x => x
        }
      }
      .recover {
        case exc => Seq(Left(s"${this.summary} ${exc.toString}"))
      }

  final def piped(result: ExtractionResult): Extractor =
    result match {
      case Left(value)  => new FailFastExtractor(value, config)
      case Right(value) => toPipedExtractor(value)
    }

  final lazy val throttling =
    config
      .getOptDuration("throttle")
      .map(d => FiniteDuration(d.toNanos, TimeUnit.NANOSECONDS))

  private def concatValues(orig: Extracted, other: Extracted): Extracted =
    orig.concat(
      other
        .map {
          case (key, value) => if (orig.contains(key)) resolve(key) -> value else key -> value
        }
    )

  private def resolve(key: String): String =
    conflictResolution match {
      case Substitute   => key
      case Prepend(str) => s"$str$key"
      case Append(str)  => s"$key$str"
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

sealed trait ConflictResolution

case object Substitute extends ConflictResolution
case class Prepend(str: String) extends ConflictResolution
case class Append(str: String) extends ConflictResolution
