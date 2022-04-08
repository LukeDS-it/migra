package it.ldsoftware.starling.engine.extractors

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.engine.ProcessContext
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class ExtractorSpec extends AnyWordSpec with should.Matchers with ScalaFutures with IntegrationPatience {

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
      val pc = ProcessContext(ActorSystem("test"))
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
      val pc = ProcessContext(ActorSystem("test"))
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
      val pc = ProcessContext(ActorSystem("test"))
      val extractor = ExtractorFactory.getExtractors(c, pc).head.toPipedExtractor(data)

      val expected = Map("old" -> "value", "extracted" -> "template string")

      extractor.extract().futureValue shouldBe Seq(Right(expected))
    }
  }

}
