package it.ldsoftware.starling.engine.consumers

import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConsumerFactorySpec extends AnyWordSpec with Matchers {

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
      val consumers = ConsumerFactory.getConsumers(c, null, null)

      consumers should have size 1

      val consumer = consumers.head
      consumer should be(a[DummyConsumer])
      consumer.asInstanceOf[DummyConsumer].parameter shouldBe "template string"
    }

  }

}
