package it.ldsoftware.migra.engine.extractors

import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.migra.engine.{Extracted, ExtractionResult, Extractor, ExtractorBuilder, ProcessContext}
import it.ldsoftware.migra.engine.extractors.FilterExtractor.Matcher

import scala.concurrent.{ExecutionContext, Future}

class FilterExtractor(
    property: String,
    matcher: Matcher,
    override val config: Config,
    override val initialValue: Extracted
)(implicit
    val ec: ExecutionContext,
    mat: Materializer
) extends Extractor {

  override def doExtract(): Future[Seq[ExtractionResult]] =
    Future(initialValue)
      .map(_.get(property))
      .map {
        case Some(value) => if (matcher.matches(value)) Seq(Right(initialValue)) else Seq()
        case None        => Seq()
      }

  override def toPipedExtractor(data: Extracted): Extractor =
    new FilterExtractor(property, matcher, config, data)

  override def summary: String =
    s"FilterExtractor with matcher ${matcher.representation(initialValue(property))}"

}

object FilterExtractor extends ExtractorBuilder {

  override def apply(config: Config, pc: ProcessContext): Extractor = {
    val property = config.getString("property")
    val matcher = config.getConfig("matcher").getString("op") match {
      case "=="  => new EqualsTo(config.getConfig("matcher").getAnyRef("to"))
      case "!=" => new NotEqualTo(config.getConfig("matcher").getAnyRef("to"))
      case ">"  => new GreaterThan(config.getConfig("matcher").getAnyRef("to"))
      case "<"  => new LowerThan(config.getConfig("matcher").getAnyRef("to"))
      case x    => throw new Error(s"Matcher for $x is not supported (yet?)")
    }

    implicit val executionContext: ExecutionContext = pc.executionContext
    implicit val materializer: Materializer = pc.materializer

    new FilterExtractor(property, matcher, config, Map())
  }

  sealed trait Matcher {
    def matches(value: Any): Boolean
    def representation(value: Any): String
  }

  class EqualsTo(other: Any) extends Matcher {
    override def matches(value: Any): Boolean = value.equals(other)

    override def representation(value: Any): String =
      s"input ($value) must be equals to $other"
  }

  class NotEqualTo(other: Any) extends Matcher {
    override def matches(value: Any): Boolean = !value.equals(other)

    override def representation(value: Any): String =
      s"input ($value) must be different from $other"
  }

  class GreaterThan(other: Any) extends Matcher {
    override def matches(value: Any): Boolean =
      value.toString.compareTo(other.toString) > 0

    override def representation(value: Any): String =
      s"input ($value) must be greater than $other"
  }

  class LowerThan(other: Any) extends Matcher {
    override def matches(value: Any): Boolean =
      value.toString.compareTo(other.toString) < 0

    override def representation(value: Any): String =
      s"input ($value) must be lower than $other"
  }
}
