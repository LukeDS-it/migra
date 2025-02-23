package it.ldsoftware.migra.engine.extractors

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.client.{BasicCredentials, WireMock}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.*
import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{Extractor, FileResolver, ProcessContext, TokenProvider}
import org.mockito.IdiomaticMockito
import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.mutable
import scala.concurrent.Future

class HttpExtractorSpec
    extends AnyWordSpec
    with GivenWhenThen
    with Matchers
    with IdiomaticMockito
    with ScalaFutures
    with IntegrationPatience {

  "extract" should {
    "get the whole response as extraction result" in new Fixture {

      // language=JSON
      override val config: String =
        s"""
          |{
          | "url": "http://localhost:${wireMock.port()}"
          |}
          |""".stripMargin

      // language=JSON
      private val json =
        """
          |{
          |  "strField": "string",
          |  "intField": 10
          |}
          |""".stripMargin

      stubFor(get("/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)))

      private val expected = Seq(Right(Map("strField" -> "string", "intField" -> 10)))

      subject.extract().futureValue shouldBe expected
    }

    "get the whole response as list of extraction result" in new Fixture {

      // language=JSON
      override val config: String =
        s"""
           |{
           | "url": "http://localhost:${wireMock.port()}"
           |}
           |""".stripMargin

      // language=JSON
      private val json =
        """[
          |  {
          |    "strField": "string",
          |    "intField": 10
          |  }
          |]
          |""".stripMargin

      stubFor(get("/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)))

      private val expected = Seq(Right(Map("strField" -> "string", "intField" -> 10)))

      subject.extract().futureValue shouldBe expected
    }

    "get a sub-property of the response as list of extraction result" in new Fixture {
      val subProp = "content"

      // language=JSON
      override val config: String =
        s"""
          |{
          | "url": "http://localhost:${wireMock.port()}",
          | "subPath": "$subProp"
          |}
          |""".stripMargin

      // language=JSON
      private val json =
        """
           |{
           |  "page": 1,
           |  "content": [
           |    {"name": "user1"},
           |    {"name": "user2"}
           |  ]
           |}
           |""".stripMargin

      private val expected = Seq(
        Right(Map("name" -> "user1")),
        Right(Map("name" -> "user2"))
      )

      stubFor(get("/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)))

      subject.extract().futureValue shouldBe expected
    }

    "get a sub-property of the response as extraction result" in new Fixture {
      val subProp = "credentials"

      // language=JSON
      override val config: String =
        s"""
          |{
          | "url": "http://localhost:${wireMock.port()}",
          | "subPath": "$subProp"
          |}
          |""".stripMargin

      // language=JSON
      private val json =
        """
          |{
          |  "application": "migra",
          |  "credentials": {
          |    "accessKey": "AKIAI0",
          |    "secretKey": "WUBRG~"
          |  }
          |}
          |""".stripMargin

      private val expected = Map("accessKey" -> "AKIAI0", "secretKey" -> "WUBRG~")

      stubFor(get("/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)))

      subject.extract().futureValue shouldBe Seq(Right(expected))
    }

    "call URLs with basic auth credentials" in new Fixture {
      // language=JSON
      override val config: String =
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

      // language=JSON
      private val json =
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

      private val expected = Seq(Right(Map("strField" -> "string", "intField" -> 10)))

      subject.extract().futureValue shouldBe expected

      verify(getRequestedFor(urlEqualTo("/")).withBasicAuth(new BasicCredentials("user", "pass")))
    }

    "call URLs with a static bearer token" in new Fixture {
      // language=JSON
      override val config: String =
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

      // language=JSON
      private val json =
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

      private val expected = Seq(Right(Map("strField" -> "string", "intField" -> 10)))

      subject.extract().futureValue shouldBe expected

      verify(getRequestedFor(urlEqualTo("/")).withHeader("Authorization", equalTo("Bearer token")))
    }

    "call URLs with custom authentication" in new Fixture {

      // language=JSON
      override val config: String =
        s"""
           |{
           | "url": "http://localhost:${wireMock.port()}",
           | "auth": {
           |   "type": "custom",
           |   "schema": "schema",
           |   "token": "token"
           | }
           |}
           |""".stripMargin

      // language=JSON
      private val json =
        """
          |{
          |  "strField": "string",
          |  "intField": 10
          |}
          |""".stripMargin

      stubFor(
        get("/")
          .withHeader("Authorization", equalTo("schema token"))
          .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json))
      )

      private val expected = Seq(Right(Map("strField" -> "string", "intField" -> 10)))

      subject.extract().futureValue shouldBe expected

      verify(getRequestedFor(urlEqualTo("/")).withHeader("Authorization", equalTo("schema token")))
    }

    "call URLs with an OAuth2 token provided by a cached provider" in new Fixture {
      Given("a configuration for an http extractor that requires a token provider")
      // language=JSON
      override val config: String =
        s"""
           |{
           | "url": "http://localhost:${wireMock.port()}",
           | "auth": {
           |   "type": "oauth2",
           |   "provider": "$providerName"
           | }
           |}
           |""".stripMargin

      And("a mocked provider that returns tokens")
      mockedProvider.token returns Future.successful("token")

      And("response data")
      // language=JSON
      private val json =
        """
          |{
          |  "strField": "string",
          |  "intField": 10
          |}
          |""".stripMargin
      private val expected = Seq(Right(Map("strField" -> "string", "intField" -> 10)))

      stubFor(
        get("/")
          .withHeader("Authorization", equalTo("Bearer token"))
          .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json))
      )

      When("extracting the data")
      subject.extract().futureValue shouldBe expected

      Then("the request should have the token in the authorization header")
      verify(getRequestedFor(urlEqualTo("/")).withHeader("Authorization", equalTo("Bearer token")))
    }

    "call URLs with POST and body" in new Fixture {
      Given("A configuration with POST method and a json request body")

      // language=JSON
      override val config: String =
        s"""
           |{
           |  "url": "http://localhost:${wireMock.port()}",
           |  "method": "POST",
           |  "body": {
           |    "key": "value"
           |  }
           |}
           |""".stripMargin

      // language=JSON
      val expectedJson = """{"key": "value"}"""

      And("response data")
      // language=JSON
      private val json =
        """
          |{
          |  "strField": "string",
          |  "intField": 10
          |}
          |""".stripMargin
      private val expected = Seq(Right(Map("strField" -> "string", "intField" -> 10)))

      stubFor(
        post("/")
          .withRequestBody(equalToJson(expectedJson, true, true))
          .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json))
      )

      When("extracting the data")
      Then("the request should be properly executed")
      subject.extract().futureValue shouldBe expected
    }
  }

  private trait Fixture {
    val config: String
    val wireMock: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())
    val providerName: String = "mockedProvider"

    wireMock.start()
    WireMock.configureFor(wireMock.port())
    val mockedProvider: TokenProvider = mock[TokenProvider]

    private val system: ActorSystem = ActorSystem("test-http-extractor")
    private val pc: ProcessContext = ProcessContext(system, mock[AppConfig], mock[FileResolver], mutable.Map(providerName -> mockedProvider))

    lazy val subject: Extractor = HttpExtractor(ConfigFactory.parseString(config), pc)
  }
}
