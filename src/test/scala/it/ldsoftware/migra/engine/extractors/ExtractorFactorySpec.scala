package it.ldsoftware.migra.engine.extractors

import org.apache.pekko.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{FileResolver, ProcessContext}
import org.mockito.IdiomaticMockito
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ExtractorFactorySpec extends AnyWordSpec with Matchers with IdiomaticMockito {

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
      val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], mock[FileResolver])
      val extractors = ExtractorFactory.getExtractors(c, pc)

      extractors should have size 1

      val consumer = extractors.head
      consumer should be(a[DummyExtractor])
      consumer.asInstanceOf[DummyExtractor].parameter shouldBe "template string"
    }

  }

}
