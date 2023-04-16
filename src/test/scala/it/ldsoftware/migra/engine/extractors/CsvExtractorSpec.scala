package it.ldsoftware.migra.engine.extractors

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{Extractor, FileResolver, ProcessContext}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CsvExtractorSpec extends AnyWordSpec with Matchers with MockFactory with ScalaFutures with IntegrationPatience {

  "extract" should {
    "retrieve rows from a csv file, assigning data to corresponding header" in new Fixture {
      // language=csv
      private val file =
        """header1,header2
          |value1,value2
          |""".stripMargin
      private val expected = Map("header1" -> "value1", "header2" -> "value2")

      private val fileName = "file.csv"

      // language=json
      private val configJson =
        s"""
           |{
           |  "file": "$fileName"
           |}""".stripMargin

      override val config: Config = ConfigFactory.parseString(configJson)

      (fileResolver.retrieveFile _).expects(fileName).returning(file)

      subject.extract().futureValue shouldBe Seq(Right(expected))
    }

    "work with a different separator" in new Fixture {
      // language=csv
      private val file =
        """header1;header2
          |value1;value2
          |""".stripMargin
      private val expected = Map("header1" -> "value1", "header2" -> "value2")

      private val fileName = "file.csv"

      // language=json
      private val configJson =
        s"""
           |{
           |   "file": "$fileName",
           |   "separator": ";"
           |}""".stripMargin

      override val config: Config = ConfigFactory.parseString(configJson)

      (fileResolver.retrieveFile _).expects(fileName).returning(file)

      subject.extract().futureValue shouldBe Seq(Right(expected))
    }

    "return an empty list when there is only a header" in new Fixture {
      // language=csv
      private val file =
        """header1,header2
          |""".stripMargin

      private val fileName = "file.csv"

      // language=json
      private val configJson =
        s"""
           |{
           |  "file": "$fileName"
           |}""".stripMargin

      override val config: Config = ConfigFactory.parseString(configJson)

      (fileResolver.retrieveFile _).expects(fileName).returning(file)

      subject.extract().futureValue shouldBe Seq()
    }
  }

  private trait Fixture {
    val config: Config
    val fileResolver: FileResolver = mock[FileResolver]
    private val system = ActorSystem("test-csv-extractor")
    private val pc = ProcessContext(system, mock[AppConfig], fileResolver)

    lazy val subject: Extractor = CsvExtractor(config, pc)
  }

}
