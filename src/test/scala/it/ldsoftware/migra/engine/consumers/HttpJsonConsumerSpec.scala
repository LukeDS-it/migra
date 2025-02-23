package it.ldsoftware.migra.engine.consumers

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{Consumed, Consumer, FileResolver, ProcessContext}
import org.mockito.IdiomaticMockito
import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HttpJsonConsumerSpec
    extends AnyWordSpec
    with GivenWhenThen
    with Matchers
    with IdiomaticMockito
    with ScalaFutures
    with IntegrationPatience {

  "consumeSuccess" should {
    "send a POST request with the compiled json" in new Fixture {

      private val url = s"http://localhost:${wireMock.port()}"
      private val template = """{\"param1\": \"${string}\", \"param2\": ${number}}"""

      // language=JSON
      override val config: String =
        s"""
          |{
          |  "url": "$url",
          |  "template": "$template"
          |}
          |""".stripMargin

      private val data = Map("string" -> "string", "number" -> 1)

      stubFor(post("/").willReturn(aResponse().withStatus(201)))

      private val expectedJson = """{"param1": "string", "param2": 1}"""

      subject.consumeSuccess(data).futureValue shouldBe Consumed(s"POST $url with $expectedJson executed with success")

      verify(
        postRequestedFor(urlEqualTo("/"))
          .withHeader("Content-Type", equalTo("application/json"))
          .withRequestBody(matchingJsonPath("$.param1", equalTo("string")))
          .withRequestBody(matchingJsonPath("$.param2", equalTo("1")))
      )
    }

    "send a request with custom method" in new Fixture {
      private val url = s"http://localhost:${wireMock.port()}"
      private val template = """{\"param1\": \"${string}\", \"param2\": ${number}}"""

      // language=JSON
      override val config: String =
        s"""
           |{
           |  "url": "$url",
           |  "template": "$template",
           |  "method": "PUT"
           |}
           |""".stripMargin

      private val data = Map("string" -> "string", "number" -> 1)

      stubFor(put("/").willReturn(aResponse().withStatus(201)))

      private val expectedJson = """{"param1": "string", "param2": 1}"""

      subject.consumeSuccess(data).futureValue shouldBe Consumed(s"PUT $url with $expectedJson executed with success")

      verify(
        putRequestedFor(urlEqualTo("/"))
          .withHeader("Content-Type", equalTo("application/json"))
          .withRequestBody(matchingJsonPath("$.param1", equalTo("string")))
          .withRequestBody(matchingJsonPath("$.param2", equalTo("1")))
      )
    }

    "send a request to a dynamic URL" in new Fixture {
      private val url = s"http://localhost:${wireMock.port()}"
      private val template = """{\"param1\": \"${string}\", \"param2\": ${number}}"""

      // language=JSON
      override val config: String =
        s"""
           |{
           |  "url": "$url/dynamic/$${param}",
           |  "template": "$template",
           |  "method": "PUT"
           |}
           |""".stripMargin

      private val data = Map("string" -> "string", "number" -> 1, "param" -> "extra")

      stubFor(put("/dynamic/extra").willReturn(aResponse().withStatus(201)))

      private val expectedJson = """{"param1": "string", "param2": 1}"""

      subject.consumeSuccess(data).futureValue shouldBe Consumed(
        s"PUT $url/dynamic/extra with $expectedJson executed with success"
      )

      verify(
        putRequestedFor(urlEqualTo("/dynamic/extra"))
          .withHeader("Content-Type", equalTo("application/json"))
          .withRequestBody(matchingJsonPath("$.param1", equalTo("string")))
          .withRequestBody(matchingJsonPath("$.param2", equalTo("1")))
      )

    }
  }

  private trait Fixture {
    val config: String

    val wireMock: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())
    wireMock.start()
    WireMock.configureFor(wireMock.port())

    private val system: ActorSystem = ActorSystem("test-http-consumer")
    private val pc: ProcessContext = ProcessContext(system, mock[AppConfig], mock[FileResolver])

    lazy val subject: Consumer = HttpJsonConsumer(ConfigFactory.parseString(config), pc)
  }

}
