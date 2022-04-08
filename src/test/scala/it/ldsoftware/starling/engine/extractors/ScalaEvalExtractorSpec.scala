package it.ldsoftware.starling.engine.extractors

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.engine.ProcessContext
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

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"))
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

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"))
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
          |         "file": "./src/test/resources/testScript.scala"
          |      }
          |    }
          |  ]
          |}""".stripMargin

      val initialData = Map("element" -> "value")

      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"))
      val extractor = ExtractorFactory.getExtractors(c, pc).head.toPipedExtractor(initialData)

      extractor.extract().futureValue shouldBe Seq(Right(Map("element" -> "value")))
    }
  }

}
