package it.ldsoftware.migra.extensions

import it.ldsoftware.migra.extensions.Interpolator.*
import org.mockito.IdiomaticMockito
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.sql.Connection

class InterpolatorSpec extends AnyWordSpec with Matchers with IdiomaticMockito {

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

  "The connection interpolator" should {
    "Create a positional query from a named query" in {
      val connection = mock[Connection]

      val testQuery = "insert into table values (:id, :name, :surname, :phoneNumber);"
      val expectedQuery = "insert into table values (?, ?, ?, ?);"
      val expectedParams = Map("id" -> 1, "name" -> 2, "surname" -> 3, "phoneNumber" -> 4)

      val subject = new ExtendedConnection(connection)
      val (query, params) = subject.getPositionalQuery(testQuery)

      query shouldBe expectedQuery
      params shouldBe expectedParams
    }
  }

}
