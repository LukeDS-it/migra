package it.ldsoftware.starling.engine.util

import it.ldsoftware.starling.DatabaseUtils
import org.apache.commons.lang3.RandomStringUtils
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

//noinspection SqlDialectInspection,SqlNoDataSourceInspection
class ReflectionFactoryIntSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience {

  import slick.jdbc.H2Profile.api._

  "getDatabase" should {
    val jdbcDriver = "org.h2.Driver"

    "return a database connection" in {
      val jdbcUrl = s"jdbc:h2:mem:${RandomStringUtils.randomAlphanumeric(10)}"
      val db = DatabaseUtils.getDatabase(jdbcUrl, jdbcDriver)

      db.run(sql"select 1 from dual".as[Int]).futureValue shouldBe Vector(1)
    }

    "accept credentials" in {
      val jdbcUrl = s"jdbc:h2:mem:${RandomStringUtils.randomAlphanumeric(10)}"
      val user = "username"
      val pass = "password"
      val db = DatabaseUtils.getDatabase(jdbcUrl, jdbcDriver, user, pass)

      db.run(sql"select 1 from dual".as[Int]).futureValue shouldBe Vector(1)
    }

  }

}
