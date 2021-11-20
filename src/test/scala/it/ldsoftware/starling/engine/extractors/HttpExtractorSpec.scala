package it.ldsoftware.starling.engine.extractors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import it.ldsoftware.starling.engine.extractors.HttpExtractor.NoAuth
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

class HttpExtractorSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience {

  private val wireMock = new WireMockServer(wireMockConfig().dynamicPort())
  wireMock.start()
  WireMock.configureFor(wireMock.port())

  private val system = ActorSystem("test-http-extractor")
  private implicit val mat: Materializer = Materializer(system)
  private implicit val ec: ExecutionContext = system.dispatcher
  private val http = Http(system)
  private val baseUrl = s"http://localhost:${wireMock.port()}"

  "extract" should {
    "get the whole response as extraction result" in {

      //language=JSON
      val json =
        """
          |{
          |  "strField": "string",
          |  "intField": 10
          |}
          |""".stripMargin

      stubFor(get("/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)))

      val expected = Seq(Right(Map("strField" -> "string", "intField" -> 10)))

      val subject = new HttpExtractor(baseUrl, None, NoAuth, http)

      subject.extract().futureValue shouldBe expected
    }

    "get a sub-property of the response as list of extraction result" in {
      val subProp = "content"

      //language=JSON
      val json =
        """
           |{
           |  "page": 1,
           |  "content": [
           |    {"name": "user1"},
           |    {"name": "user2"}
           |  ]
           |}
           |""".stripMargin

      val expected = Seq(
        Right(Map("name" -> "user1")),
        Right(Map("name" -> "user2"))
      )

      stubFor(get("/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)))

      val subject = new HttpExtractor(baseUrl, Some(subProp), NoAuth, http)

      subject.extract().futureValue shouldBe expected
    }

    "get a sub-property of the response as extraction result" in {
      val subProp = "credentials"

      //language=JSON
      val json =
        """
          |{
          |  "application": "starling-migrate",
          |  "credentials": {
          |    "accessKey": "AKIAI0",
          |    "secretKey": "WUBRG~"
          |  }
          |}
          |""".stripMargin

      val expected = Map("accessKey" -> "AKIAI0", "secretKey" -> "WUBRG~")

      stubFor(get("/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)))

      val subject = new HttpExtractor(baseUrl, Some(subProp), NoAuth, http)

      subject.extract().futureValue shouldBe Seq(Right(expected))
    }
  }

}
