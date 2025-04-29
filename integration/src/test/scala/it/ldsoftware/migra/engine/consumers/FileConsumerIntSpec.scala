package it.ldsoftware.migra.engine.consumers

import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.ProcessContext
import it.ldsoftware.migra.engine.resolvers.LocalFileResolver
import it.ldsoftware.migra.extensions.UsableExtensions.UsableCloseable
import org.apache.pekko.actor.ActorSystem
import org.mockito.IdiomaticMockito
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import scala.io.Source

class FileConsumerIntSpec extends AnyWordSpec
  with Matchers
  with Eventually
  with ScalaFutures
  with IntegrationPatience
  with IdiomaticMockito
  with BeforeAndAfterEach {

  val outputFile = "file-consumer-output.txt"

  override def afterEach(): Unit = {
    new File("src/test/resources/$outputFile").delete()
  }

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(15, Millis)))

  private val fileResolver = new LocalFileResolver(new File("src/test/resources/test-db-to-db.json"))

  private val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], fileResolver)

  "it" should {
    "output data on a file adding a header" in {
      //language=json
      val config =
        s"""{
           |  "file": "$outputFile",
           |  "header": "header",
           |  "template": "$${value}"
           |}
           |""".stripMargin

      val subject: FileConsumer = FileConsumer(ConfigFactory.parseString(config), pc).asInstanceOf[FileConsumer]

      subject.consumeSuccess(Map("value" -> "value1")).futureValue
      subject.consumeSuccess(Map("value" -> "value2")).futureValue

      val result = Source.fromFile(s"src/test/resources/$outputFile").use(_.getLines().mkString("\n"))
      result shouldBe
        """header
          |value1
          |value2""".stripMargin
    }
  }

}
