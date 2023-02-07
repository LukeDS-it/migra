package it.ldsoftware.migra.extensions

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import JacksonExtension._

class JacksonExtensionSpec extends AnyWordSpec with Matchers {

  "asGeneric" should {
    "convert a json string into a map of values" in {
      // language=JSON
      val json =
        """
          |{
          |  "string": "abc",
          |  "num": 123
          |}
          |""".stripMargin

      json.asMap shouldBe Map("string" -> "abc", "num" -> 123)
    }
  }

  "subGeneric" should {
    "return a list of maps when the sub-property references a collection" in {
      // language=JSON
      val json =
        """
          |{
          |  "collection": [
          |     {"name": "user1"},
          |     {"name": "user2"}
          |  ]
          |}
          |""".stripMargin

      val user1 = Map("name" -> "user1")
      val user2 = Map("name" -> "user2")

      json.jsonGet("collection") shouldBe SubArray(Seq(user1, user2))
    }

    "return a map of values when the sub-property references an object" in {
      // language=JSON
      val json =
        """
          |{
          |  "user": {"name":  "user1"}
          |}
          |""".stripMargin

      val user1 = Map("name" -> "user1")

      json.jsonGet("user") shouldBe SubGeneric(user1)
    }

    "return an empty map when the property does not exist" in {
      // language=JSON
      val json =
        """
          |{
          |  "user": {"name":  "user1"}
          |}
          |""".stripMargin

      val empty = Map[String, Any]()

      json.jsonGet("credentials") shouldBe SubGeneric(empty)
    }
  }

}
