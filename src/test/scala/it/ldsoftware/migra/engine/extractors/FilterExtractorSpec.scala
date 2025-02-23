package it.ldsoftware.migra.engine.extractors

import org.apache.pekko.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{FileResolver, ProcessContext}
import org.mockito.IdiomaticMockito
import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FilterExtractorSpec
    extends AnyWordSpec
    with GivenWhenThen
    with Matchers
    with IdiomaticMockito
    with ScalaFutures
    with IntegrationPatience {

  private val system = ActorSystem("test-filter-extractor")
  private val pc = ProcessContext(system, mock[AppConfig], mock[FileResolver])

  "the filter extractor configured with equals" should {
    // language=JSON
    val config =
      """
        |{
        |  "property": "id",
        |  "matcher": {
        |     "op": "==",
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
        |     "op": "!=",
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

  "the filter extractor configured with greater than" should {
    // language=JSON
    val config =
      """
        |{
        |  "property": "id",
        |  "matcher": {
        |     "op": ">",
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

  "the filter extractor configured with lower than" should {
    // language=JSON
    val config =
      """
        |{
        |  "property": "id",
        |  "matcher": {
        |     "op": "<",
        |     "to": 2
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

}
