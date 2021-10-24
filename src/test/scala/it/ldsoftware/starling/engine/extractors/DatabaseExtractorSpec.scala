package it.ldsoftware.starling.engine.extractors

import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3.RandomStringUtils
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration._

//noinspection SqlNoDataSourceInspection
class DatabaseExtractorSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience {

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

      val steak = Right(Map("id" -> 1, "name" -> "steak", "price" -> 1000))
      val broth = Right(Map("id" -> 2, "name" -> "broth", "price" -> 1250))
      val bread = Right(Map("id" -> 3, "name" -> "bread", "price" -> 250))
      val yogurt = Right(Map("id" -> 4, "name" -> "yogurt", "price" -> 199))

      val subject = DatabaseExtractor(ConfigFactory.parseString(config))

      subject.extract().futureValue should contain allElementsOf Seq(steak, broth, bread, yogurt)
    }

  }

}
