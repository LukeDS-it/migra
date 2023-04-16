package it.ldsoftware.migra.engine.extractors

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{FileResolver, ProcessContext}
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
    "emit new data when the configuration does not specify anything" in {
      // language=JSON
      val config =
        """{
          |  "extract":  [
          |    {
          |      "type":  "DummyExtractor",
          |      "config": {"parameter":  "template string"}
          |    }
          |  ]
          |}""".stripMargin

      val data = Map("old" -> "value")

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], mock[FileResolver])
      val extractor = ExtractorFactory.getExtractors(c, pc).head.toPipedExtractor(data)

      extractor.extract().futureValue shouldBe Seq(Right(Map("extracted" -> "template string")))
    }

    "emit new data when the configuration explicitly says to replace" in {
      // language=JSON
      val config =
        """{
          |  "extract":  [
          |    {
          |      "type":  "DummyExtractor",
          |      "config": {"parameter":  "template string", "mode": "replace"}
          |    }
          |  ]
          |}""".stripMargin

      val data = Map("old" -> "value")

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], mock[FileResolver])
      val extractor = ExtractorFactory.getExtractors(c, pc).head.toPipedExtractor(data)

      extractor.extract().futureValue shouldBe Seq(Right(Map("extracted" -> "template string")))
    }

    "emit interpolated data when the configuration is to merge data" in {
      // language=JSON
      val config =
        """{
          |  "extract":  [
          |    {
          |      "type":  "DummyExtractor",
          |      "config": {"parameter":  "template string", "mode": "merge"}
          |    }
          |  ]
          |}""".stripMargin

      val data = Map("old" -> "value")

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], mock[FileResolver])
      val extractor = ExtractorFactory.getExtractors(c, pc).head.toPipedExtractor(data)

      val expected = Map("old" -> "value", "extracted" -> "template string")

      extractor.extract().futureValue shouldBe Seq(Right(expected))
    }

    "prepend a value to the new extracted property name when conflict resolving is Prepend" in {
      // language=JSON
      val config =
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

      val data = Map("extracted" -> "value")

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], mock[FileResolver])
      val extractor = ExtractorFactory.getExtractors(c, pc).head.toPipedExtractor(data)

      val expected = Map("extracted" -> "value", "new_extracted" -> "template string")

      extractor.extract().futureValue shouldBe Seq(Right(expected))
    }

    "append a value to the new extracted property name when conflict resolving is Append" in {
      // language=JSON
      val config =
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

      val data = Map("extracted" -> "value")

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], mock[FileResolver])
      val extractor = ExtractorFactory.getExtractors(c, pc).head.toPipedExtractor(data)

      val expected = Map("extracted" -> "value", "extracted_new" -> "template string")

      extractor.extract().futureValue shouldBe Seq(Right(expected))
    }

    "substitute a value when conflict resolving is Substitute" in {
      // language=JSON
      val config =
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

      val data = Map("extracted" -> "value")

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], mock[FileResolver])
      val extractor = ExtractorFactory.getExtractors(c, pc).head.toPipedExtractor(data)

      val expected = Map("extracted" -> "template string")

      extractor.extract().futureValue shouldBe Seq(Right(expected))
    }
  }

}
