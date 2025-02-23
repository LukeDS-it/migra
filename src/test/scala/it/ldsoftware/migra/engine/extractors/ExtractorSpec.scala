package it.ldsoftware.migra.engine.extractors

import org.apache.pekko.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{Extracted, ExtractionResult, Extractor, FileResolver, ProcessContext}
import org.mockito.IdiomaticMockito
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class ExtractorSpec
    extends AnyWordSpec
    with should.Matchers
    with ScalaFutures
    with IntegrationPatience
    with IdiomaticMockito {

  "an extractor" should {
    "emit new data when the configuration does not specify anything" in new Fixture {
      // language=JSON
      override val config: String =
        """{
          |  "extract":  [
          |    {
          |      "type":  "DummyExtractor",
          |      "config": {"parameter":  "template string"}
          |    }
          |  ]
          |}""".stripMargin

      override val data: Extracted = Map("old" -> "value")

      override val expected: Seq[ExtractionResult] = Seq(Right(Map("extracted" -> "template string")))

      subject.extract().futureValue shouldBe expected
    }

    "emit new data when the configuration explicitly says to replace" in new Fixture {
      // language=JSON
      override val config: String =
        """{
          |  "extract":  [
          |    {
          |      "type":  "DummyExtractor",
          |      "config": {"parameter":  "template string", "mode": "replace"}
          |    }
          |  ]
          |}""".stripMargin

      override val data: Extracted = Map("old" -> "value")

      override val expected: Seq[ExtractionResult] = Seq(Right(Map("extracted" -> "template string")))

      subject.extract().futureValue shouldBe expected
    }

    "emit interpolated data when the configuration is to merge data" in new Fixture {
      // language=JSON
      override val config: String =
        """{
          |  "extract":  [
          |    {
          |      "type":  "DummyExtractor",
          |      "config": {"parameter":  "template string", "mode": "merge"}
          |    }
          |  ]
          |}""".stripMargin

      override val data: Extracted = Map("old" -> "value")


      override val expected: Seq[ExtractionResult] = Seq(Right(Map("old" -> "value", "extracted" -> "template string")))

      subject.extract().futureValue shouldBe expected
    }

    "prepend a value to the new extracted property name when conflict resolving is Prepend" in new Fixture {
      // language=JSON
      override val config: String =
        """{
          |  "extract":  [
          |    {
          |      "type":  "DummyExtractor",
          |      "config": {
          |        "parameter": "template string",
          |        "mode": "merge",
          |        "conflict": {
          |           "action": "prepend",
          |           "value": "new_"
          |        }
          |      }
          |    }
          |  ]
          |}""".stripMargin

      override val data: Extracted = Map("extracted" -> "value")

      override val expected: Seq[ExtractionResult] = Seq(Right(Map("extracted" -> "value", "new_extracted" -> "template string")))

      subject.extract().futureValue shouldBe expected
    }

    "append a value to the new extracted property name when conflict resolving is Append" in new Fixture {
      // language=JSON
      override val config: String =
        """{
          |  "extract":  [
          |    {
          |      "type":  "DummyExtractor",
          |      "config": {
          |        "parameter": "template string",
          |        "mode": "merge",
          |        "conflict": {
          |           "action": "append",
          |           "value": "_new"
          |        }
          |      }
          |    }
          |  ]
          |}""".stripMargin

      override val data: Extracted = Map("extracted" -> "value")

      override val expected: Seq[ExtractionResult] = Seq(Right(Map("extracted" -> "value", "extracted_new" -> "template string")))

      subject.extract().futureValue shouldBe expected
    }

    "substitute a value when conflict resolving is Substitute" in new Fixture {
      // language=JSON
      override val config: String =
        """{
          |  "extract":  [
          |    {
          |      "type":  "DummyExtractor",
          |      "config": {
          |        "parameter": "template string",
          |        "mode": "merge",
          |        "conflict": {
          |           "action": "substitute"
          |        }
          |      }
          |    }
          |  ]
          |}""".stripMargin

      override val data: Extracted = Map("extracted" -> "value")

      override val expected: Seq[ExtractionResult] = Seq(Right(Map("extracted" -> "template string")))

      subject.extract().futureValue shouldBe expected
    }
  }

  private trait Fixture {
    val config: String
    val data: Extracted
    val expected: Seq[ExtractionResult]

    private lazy val c = ConfigFactory.parseString(config)
    private val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], mock[FileResolver])

    lazy val subject: Extractor = ExtractorFactory.getExtractors(c, pc).head.toPipedExtractor(data)
  }

}
