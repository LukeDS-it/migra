package it.ldsoftware.starling.engine.util

import it.ldsoftware.starling.engine.util.Interpolator._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class InterpolatorSpec extends AnyWordSpec with Matchers {

  "The main interpolator" should {

    "Interpolate strings with values coming from a map" in {
      val input = "update ${table} set ${field} = '${value}' where id = ${id}"
      val map = Map(
        "table" -> "users",
        "field" -> "name",
        "value" -> "Luca",
        "id" -> 10
      )

      input <-- map shouldBe "update users set name = 'Luca' where id = 10"
    }

    "Interpolate the string values in a map" in {
      val input = Map(
        "string" -> "this ${objectName} should be interpolated",
        "number" -> 10,
        "others" -> "another ${objectName} that must be interpolated"
      )

      val map = Map("objectName" -> "string")

      input <-- map shouldBe Map(
        "string" -> "this string should be interpolated",
        "number" -> 10,
        "others" -> "another string that must be interpolated"
      )
    }

  }

}
