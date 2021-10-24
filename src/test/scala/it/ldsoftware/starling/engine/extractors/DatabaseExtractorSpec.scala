package it.ldsoftware.starling.engine.extractors

import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3.RandomStringUtils
import org.scalatest.matchers.should
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.Await
import scala.concurrent.duration._

//noinspection SqlNoDataSourceInspection
class DatabaseExtractorSpec extends AsyncWordSpec with should.Matchers {

  import slick.jdbc.H2Profile.api._

  val dbName = s"/tmp/${RandomStringUtils.randomAlphanumeric(10)}"

  val db = Database.forURL(s"jdbc:h2:$dbName")

  private val execute = db.run(
    DBIO.seq(
      sqlu"create table products (id int not null, name varchar (255) not null, price bigint not null)",
      sqlu"insert into products values (1, 'steak', 1000)",
      sqlu"insert into products values (2, 'broth', 1250)",
      sqlu"insert into products values (3, 'bread', 250)",
      sqlu"insert into products values (4, 'yogurt', 199)"
    )
  )

  Await.ready(execute, 2.seconds)

  val steak = Map("id" -> 1, "name" -> "steak", "price" -> 1000)
  val broth = Map("id" -> 2, "name" -> "broth", "price" -> 1250)
  val bread = Map("id" -> 3, "name" -> "bread", "price" -> 250)
  val yogurt = Map("id" -> 4, "name" -> "yogurt", "price" -> 199)

  "it" should {

    "extract rows from a query" in {
      // language=JSON
      val config =
        s"""
          |{
          |  "query": "select * from products",
          |  "jdbc-url": "jdbc:h2:$dbName",
          |  "jdbc-driver": "org.h2.Driver"
          |}
          |""".stripMargin

      val subject = DatabaseExtractor(ConfigFactory.parseString(config))

      subject.extract() map { it =>
        it should contain allElementsOf Seq(Right(steak), Right(broth), Right(bread), Right(yogurt))
      }
    }

  }

}
