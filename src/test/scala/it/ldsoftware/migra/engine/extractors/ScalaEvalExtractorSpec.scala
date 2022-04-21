package it.ldsoftware.migra.engine.extractors

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{FileResolver, ProcessContext}
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ScalaEvalExtractorSpec
    extends AnyWordSpec
    with GivenWhenThen
    with Matchers
    with MockFactory
    with ScalaFutures
    with IntegrationPatience {

  "extract" should {
    "produce data" in {
      // language=JSON
      val config =
        """{
          |  "extract":  [
          |    {
          |      "type":  "ScalaEvalExtractor",
          |      "config": {
          |         "type": "inline",
          |         "script": "Map(\"element\" -> \"value\")"
          |      }
          |    }
          |  ]
          |}""".stripMargin

      val mockConfig = mock[Config]
      val appConfig = AppConfig(mockConfig)

      (mockConfig.getInt _).expects("it.ldsoftware.migra.max-script-engines").returning(4)

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"), appConfig, mock[FileResolver])
      val extractor = ExtractorFactory.getExtractors(c, pc).head

      extractor.extract().futureValue shouldBe Seq(Right(Map("element" -> "value")))
    }

    "use context data to produce other data" in {
      // language=JSON
      val config =
        """{
          |  "extract":  [
          |    {
          |      "type":  "ScalaEvalExtractor",
          |      "config": {
          |         "type": "inline",
          |         "script": "Map(\"element\" -> data(\"element\"))"
          |      }
          |    }
          |  ]
          |}""".stripMargin

      val initialData = Map("element" -> "value")

      val mockConfig = mock[Config]
      val appConfig = AppConfig(mockConfig)

      (mockConfig.getInt _).expects("it.ldsoftware.migra.max-script-engines").returning(4)

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"), appConfig, mock[FileResolver])
      val extractor = ExtractorFactory.getExtractors(c, pc).head.toPipedExtractor(initialData)

      extractor.extract().futureValue shouldBe Seq(Right(Map("element" -> "value")))
    }

    "read a script from a source file" in {
      // language=JSON
      val config =
        """{
          |  "extract":  [
          |    {
          |      "type":  "ScalaEvalExtractor",
          |      "config": {
          |         "type": "file",
          |         "file": "testScript.scala"
          |      }
          |    }
          |  ]
          |}""".stripMargin

      val initialData = Map("element" -> "value")

      val mockConfig = mock[Config]
      val appConfig = AppConfig(mockConfig)

      val fileResolver = mock[FileResolver]
      val expectedScript =
        """
          |def produce(data: Map[String, Any]): Map[String, Any] = Map("element" -> data("element"))
          |""".stripMargin

      (mockConfig.getInt _).expects("it.ldsoftware.migra.max-script-engines").returning(4)
      (fileResolver.retrieveFile _).expects("testScript.scala").returning(expectedScript)

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"), appConfig, fileResolver)
      val extractor = ExtractorFactory.getExtractors(c, pc).head.toPipedExtractor(initialData)

      extractor.extract().futureValue shouldBe Seq(Right(Map("element" -> "value")))
    }
  }

}
