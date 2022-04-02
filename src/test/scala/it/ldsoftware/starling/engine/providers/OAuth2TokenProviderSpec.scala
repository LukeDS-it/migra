package it.ldsoftware.starling.engine.providers

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, containing, post, postRequestedFor, stubFor, urlEqualTo, verify}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.engine.ProcessContext
import org.apache.commons.lang3.RandomStringUtils
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class OAuth2TokenProviderSpec
    extends AnyWordSpec
    with GivenWhenThen
    with Matchers
    with MockFactory
    with ScalaFutures
    with IntegrationPatience {

  private val wireMock = new WireMockServer(wireMockConfig().dynamicPort())
  wireMock.start()
  WireMock.configureFor(wireMock.port())

  private val system = ActorSystem("test-oauth2-provider")
  private val pc = ProcessContext(system)

  "token" should {
    "get the token from an oauth2 endpoint implementation" in {
      val expectedToken = RandomStringUtils.randomAlphanumeric(10)
      val expectedMillis = Random.nextInt()
      val id = RandomStringUtils.randomAlphanumeric(10)
      val secret = RandomStringUtils.randomAlphanumeric(10)

      //language=JSON
      val config =
        s"""
           |{
           |  "endpoint": "http://localhost:${wireMock.port()}",
           |  "id": "$id",
           |  "secret": "$secret"
           |}
           |""".stripMargin

      val oAuthResponse =
        s"""
           |{
           |  "access_token": "$expectedToken",
           |  "expires_in": $expectedMillis
           |}
           |""".stripMargin

      stubFor(post("/").willReturn(aResponse().withHeader("content-Type", "application/json").withBody(oAuthResponse)))

      val subject = OAuth2TokenProvider("alias", ConfigFactory.parseString(config), pc)

      subject.token.futureValue shouldBe expectedToken

      verify(
        postRequestedFor(urlEqualTo("/"))
          .withRequestBody(containing("grant_type=client_credentials"))
          .withRequestBody(containing(s"client_id=$id"))
          .withRequestBody(containing(s"client_secret=$secret"))
      )
    }

    "get a new token if the old one is about to expire" in {
      val token1 = RandomStringUtils.randomAlphanumeric(10)
      val expiration = 9_000 // 9 seconds
      val token2 = RandomStringUtils.randomAlphanumeric(10)
      val id = RandomStringUtils.randomAlphanumeric(10)
      val secret = RandomStringUtils.randomAlphanumeric(10)

      //language=JSON
      val config =
        s"""
           |{
           |  "endpoint": "http://localhost:${wireMock.port()}",
           |  "id": "$id",
           |  "secret": "$secret"
           |}
           |""".stripMargin

      val oAuthResponse1 =
        s"""
           |{
           |  "access_token": "$token1",
           |  "expires_in": $expiration
           |}
           |""".stripMargin

      val oAuthResponse2 =
        s"""
           |{
           |  "access_token": "$token2",
           |  "expires_in": $expiration
           |}
           |""".stripMargin

      stubFor(
        post("/")
          .inScenario("refreshToken")
          .whenScenarioStateIs(Scenario.STARTED)
          .willReturn(aResponse().withHeader("content-Type", "application/json").withBody(oAuthResponse1))
          .willSetStateTo("refresh-requested")
      )

      stubFor(
        post("/")
          .inScenario("refreshToken")
          .whenScenarioStateIs("refresh-requested")
          .willReturn(aResponse().withHeader("content-Type", "application/json").withBody(oAuthResponse2))
      )

      val subject = OAuth2TokenProvider("alias", ConfigFactory.parseString(config), pc)

      subject.token.futureValue shouldBe token1

      subject.token.futureValue shouldBe token2

      verify(2, postRequestedFor(urlEqualTo("/")))
    }
  }

}
