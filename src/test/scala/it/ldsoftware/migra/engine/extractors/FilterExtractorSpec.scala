package it.ldsoftware.migra.engine.extractors

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{FileResolver, ProcessContext}
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FilterExtractorSpec  extends AnyWordSpec
  with GivenWhenThen
  with Matchers
  with MockFactory
  with ScalaFutures
  with IntegrationPatience{

  private val system = ActorSystem("test-filter-extractor")
  private val pc = ProcessContext(system, mock[AppConfig], mock[FileResolver])

  "the filter extractor configured with equals" should {
    // language=JSON
    val config =
      """
        |{
        |  "property": "id",
        |  "matcher": {
        |     "op": "equals",
        |     "to": 1
        |  }
        |}
        |""".stripMargin

    "keep values that match the predicate" in {
      val data = Map("id" -> 1)

      val subject = FilterExtractor(ConfigFactory.parseString(config), pc)
        .toPipedExtractor(data)

      subject.extract().futureValue shouldBe Seq(Right(data))
    }

    "discard values that don't match the predicate" in {
      val data = Map("id" -> 2)

      val subject = FilterExtractor(ConfigFactory.parseString(config), pc)
        .toPipedExtractor(data)

      subject.extract().futureValue shouldBe Seq()
    }
  }

  "the filter extractor configured with not equals" should {
    // language=JSON
    val config =
      """
        |{
        |  "property": "id",
        |  "matcher": {
        |     "op": "not equal",
        |     "to": 1
        |  }
        |}
        |""".stripMargin

    "keep values that match the predicate" in {
      val data = Map("id" -> 2)

      val subject = FilterExtractor(ConfigFactory.parseString(config), pc)
        .toPipedExtractor(data)

      subject.extract().futureValue shouldBe Seq(Right(data))
    }

    "discard values that don't match the predicate" in {
      val data = Map("id" -> 1)

      val subject = FilterExtractor(ConfigFactory.parseString(config), pc)
        .toPipedExtractor(data)

      subject.extract().futureValue shouldBe Seq()
    }
  }

}
