package it.ldsoftware.migra.engine.consumers

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{Consumed, FileResolver, ProcessContext}
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

  private val wireMock = new WireMockServer(wireMockConfig().dynamicPort())
  wireMock.start()
  WireMock.configureFor(wireMock.port())

  private val system = ActorSystem("test-http-consumer")
  private val pc = ProcessContext(system, mock[AppConfig], mock[FileResolver])

  "consumeSuccess" should {
    "send a POST request with the compiled json" in {

      val url = s"http://localhost:${wireMock.port()}"
      val template = """{\"param1\": \"${string}\", \"param2\": ${number}}"""

      // language=JSON
      val config =
        s"""
          |{
          |  "url": "$url",
          |  "template": "$template"
          |}
          |""".stripMargin

      val data = Map("string" -> "string", "number" -> 1)

      stubFor(post("/").willReturn(aResponse().withStatus(201)))

      val subject = HttpJsonConsumer(ConfigFactory.parseString(config), pc)

      val expectedJson = """{"param1": "string", "param2": 1}"""

      subject.consumeSuccess(data).futureValue shouldBe Consumed(s"POST $url with $expectedJson executed with success")

      verify(
        postRequestedFor(urlEqualTo("/"))
          .withHeader("Content-Type", equalTo("application/json"))
          .withRequestBody(matchingJsonPath("$.param1", equalTo("string")))
          .withRequestBody(matchingJsonPath("$.param2", equalTo("1")))
      )
    }

    "send a request with custom method" in {
      val url = s"http://localhost:${wireMock.port()}"
      val template = """{\"param1\": \"${string}\", \"param2\": ${number}}"""

      // language=JSON
      val config =
        s"""
           |{
           |  "url": "$url",
           |  "template": "$template",
           |  "method": "PUT"
           |}
           |""".stripMargin

      val data = Map("string" -> "string", "number" -> 1)

      stubFor(put("/").willReturn(aResponse().withStatus(201)))

      val subject = HttpJsonConsumer(ConfigFactory.parseString(config), pc)

      val expectedJson = """{"param1": "string", "param2": 1}"""

      subject.consumeSuccess(data).futureValue shouldBe Consumed(s"PUT $url with $expectedJson executed with success")

      verify(
        putRequestedFor(urlEqualTo("/"))
          .withHeader("Content-Type", equalTo("application/json"))
          .withRequestBody(matchingJsonPath("$.param1", equalTo("string")))
          .withRequestBody(matchingJsonPath("$.param2", equalTo("1")))
      )
    }

    "send a request to a dynamic URL" in {
      val url = s"http://localhost:${wireMock.port()}"
      val template = """{\"param1\": \"${string}\", \"param2\": ${number}}"""

      // language=JSON
      val config =
        s"""
           |{
           |  "url": "$url/dynamic/$${param}",
           |  "template": "$template",
           |  "method": "PUT"
           |}
           |""".stripMargin

      val data = Map("string" -> "string", "number" -> 1, "param" -> "extra")

      stubFor(put("/dynamic/extra").willReturn(aResponse().withStatus(201)))

      val subject = HttpJsonConsumer(ConfigFactory.parseString(config), pc)

      val expectedJson = """{"param1": "string", "param2": 1}"""

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

}
