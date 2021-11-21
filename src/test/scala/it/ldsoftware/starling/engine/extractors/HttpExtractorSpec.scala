package it.ldsoftware.starling.engine.extractors

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{BasicCredentials, WireMock}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.engine.ProcessContext
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HttpExtractorSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience {

  private val wireMock = new WireMockServer(wireMockConfig().dynamicPort())
  wireMock.start()
  WireMock.configureFor(wireMock.port())

  private val system = ActorSystem("test-http-extractor")
  private val pc = ProcessContext(system)

  "extract" should {
    "get the whole response as extraction result" in {

      //language=JSON
      val config =
        s"""
          |{
          | "url": "http://localhost:${wireMock.port()}"
          |}
          |""".stripMargin

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

      val subject = HttpExtractor(ConfigFactory.parseString(config), pc)

      subject.extract().futureValue shouldBe expected
    }

    "get a sub-property of the response as list of extraction result" in {
      val subProp = "content"

      //language=JSON
      val config =
        s"""
          |{
          | "url": "http://localhost:${wireMock.port()}",
          | "subPath": "$subProp"
          |}
          |""".stripMargin

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

      val subject = HttpExtractor(ConfigFactory.parseString(config), pc)

      subject.extract().futureValue shouldBe expected
    }

    "get a sub-property of the response as extraction result" in {
      val subProp = "credentials"

      //language=JSON
      val config =
        s"""
          |{
          | "url": "http://localhost:${wireMock.port()}",
          | "subPath": "$subProp"
          |}
          |""".stripMargin

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

      val subject = HttpExtractor(ConfigFactory.parseString(config), pc)

      subject.extract().futureValue shouldBe Seq(Right(expected))
    }

    "call URLs with basic auth credentials" in {
      //language=JSON
      val config =
        s"""
          |{
          | "url": "http://localhost:${wireMock.port()}",
          | "auth": {
          |   "type": "basic",
          |   "credentials": {
          |     "type": "plain",
          |     "user": "user",
          |     "pass": "pass"
          |   }
          | }
          |}
          |""".stripMargin

      //language=JSON
      val json =
        """
          |{
          |  "strField": "string",
          |  "intField": 10
          |}
          |""".stripMargin

      stubFor(
        get("/")
          .withBasicAuth("user", "pass")
          .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json))
      )

      val expected = Seq(Right(Map("strField" -> "string", "intField" -> 10)))

      val subject = HttpExtractor(ConfigFactory.parseString(config), pc)

      subject.extract().futureValue shouldBe expected

      verify(getRequestedFor(urlEqualTo("/")).withBasicAuth(new BasicCredentials("user", "pass")))
    }

    "call URLs with a static bearer token" in {
      //language=JSON
      val config =
        s"""
           |{
           | "url": "http://localhost:${wireMock.port()}",
           | "auth": {
           |   "type": "bearer",
           |   "credentials": {
           |     "type": "plain",
           |     "token": "token"
           |   }
           | }
           |}
           |""".stripMargin

      //language=JSON
      val json =
        """
          |{
          |  "strField": "string",
          |  "intField": 10
          |}
          |""".stripMargin

      stubFor(
        get("/")
          .withHeader("Authorization", equalTo("Bearer token"))
          .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json))
      )

      val expected = Seq(Right(Map("strField" -> "string", "intField" -> 10)))

      val subject = HttpExtractor(ConfigFactory.parseString(config), pc)

      subject.extract().futureValue shouldBe expected

      verify(getRequestedFor(urlEqualTo("/")).withHeader("Authorization", equalTo("Bearer token")))
    }

  }

}
