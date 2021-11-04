package it.ldsoftware.starling.engine.extractors

import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ExtractorFactorySpec extends AnyWordSpec with Matchers {

  // language=JSON
  private val config =
    """{
      |  "extract":  [
      |    {"type":  "DummyExtractor", "config": {"parameter":  "template string"}}
      |  ]
      |}""".stripMargin
  "getExtractors" should {

    "build the correct extractor from a configuration" in {
      val c = ConfigFactory.parseString(config)
      val extractors = ExtractorFactory.getExtractors(c, null, null)

      extractors should have size 1

      val consumer = extractors.head
      consumer should be(a[DummyExtractor])
      consumer.asInstanceOf[DummyExtractor].parameter shouldBe "template string"
    }

  }

}
