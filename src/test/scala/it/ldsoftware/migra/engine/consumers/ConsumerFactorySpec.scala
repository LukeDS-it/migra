package it.ldsoftware.migra.engine.consumers

import org.apache.pekko.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{FileResolver, ProcessContext}
import org.mockito.IdiomaticMockito
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConsumerFactorySpec extends AnyWordSpec with Matchers with IdiomaticMockito {

  // language=JSON
  private val config =
    """{
      |  "consume":  [
      |    {"type":  "DummyConsumer", "config": {"parameter":  "template string"}}
      |  ]
      |}""".stripMargin
  "getConsumers" should {

    "build the correct consumer from a configuration" in {
      val c = ConfigFactory.parseString(config)
      val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], mock[FileResolver])
      val consumers = ConsumerFactory.getConsumers(c, pc)

      consumers should have size 1

      val consumer = consumers.head
      consumer should be(a[DummyConsumer])
      consumer.asInstanceOf[DummyConsumer].parameter shouldBe "template string"
    }

  }

}
