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

class FlattenExtractorSpec extends AnyWordSpec
  with GivenWhenThen
  with Matchers
  with MockFactory
  with ScalaFutures
  with IntegrationPatience {

  private val system = ActorSystem("test-flatten-extractor")
  private val pc = ProcessContext(system, mock[AppConfig], mock[FileResolver])

  "the flatten extractor" should {
    "return a collection sub-property of given data" in {

      // language=JSON
      val config =
        """
          |{
          |  "property": "list"
          |}
          |""".stripMargin

      val data = Map("list" -> Seq(Map("a" -> "a"), Map("b" -> "b"), Map("c" -> "c")))

      val subject = FlattenExtractor(ConfigFactory.parseString(config), pc)
        .toPipedExtractor(data)

      val expected = Seq(Right(Map("a" -> "a")), Right(Map("b" -> "b")), Right(Map("c" -> "c")))

      subject.extract().futureValue shouldBe expected
    }

    "fail if the data does not contain given sub-property" in {
      // language=JSON
      val config =
        """
          |{
          |  "property": "wrong"
          |}
          |""".stripMargin

      val data = Map("key" -> "value")

      val subject = FlattenExtractor(ConfigFactory.parseString(config), pc)
        .toPipedExtractor(data)

      val expected = Seq(Left("No property wrong found on Map(key -> value)"))

      subject.extract().futureValue shouldBe expected

    }
  }

}
