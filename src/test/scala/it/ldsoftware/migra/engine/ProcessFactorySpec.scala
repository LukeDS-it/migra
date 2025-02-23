package it.ldsoftware.migra.engine

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProcessFactorySpec extends AnyWordSpec with Matchers {

  "The process factory" should {
    "support json processes" in new Fixture {
      val expected = "value"

      //language=json
      val jsonConfig: String =
        s"""{
           |  "long": {
           |    "path": {
           |      "name": "$expected"
           |    }
           |  }
           |}""".stripMargin

      subject.parseConfig(jsonConfig).getString("long.path.name") shouldBe expected
    }

    "support yaml processes" in new Fixture {
      val expected = "value"

      //language=yaml
      val yamlConfig: String =
        s"""long:
           |  path:
           |    name: $expected
           |""".stripMargin

      subject.parseConfig(yamlConfig).getString("long.path.name") shouldBe expected
    }

  }

  private class Fixture {
    val subject: ProcessFactory = new ProcessFactory(1)
  }

}
