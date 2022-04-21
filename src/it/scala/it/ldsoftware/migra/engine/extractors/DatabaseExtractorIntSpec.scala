package it.ldsoftware.migra.engine.extractors

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.DatabaseUtils
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{FileResolver, ProcessContext}
import org.apache.commons.lang3.RandomStringUtils
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration._

//noinspection SqlDialectInspection,SqlNoDataSourceInspection
class DatabaseExtractorIntSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with MockFactory {

  import slick.jdbc.H2Profile.api._

  private val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], mock[FileResolver])
  private val jdbcUrl = s"jdbc:h2:mem:${RandomStringUtils.randomAlphanumeric(10)};DB_CLOSE_DELAY=-1"
  private val db = DatabaseUtils.getDatabase(jdbcUrl, "org.h2.Driver")

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
          |  "jdbc-url": "$jdbcUrl",
          |  "jdbc-driver": "org.h2.Driver"
          |}
          |""".stripMargin

      val steak = Right(Map("id" -> 1, "name" -> "steak", "price" -> 1000))
      val broth = Right(Map("id" -> 2, "name" -> "broth", "price" -> 1250))
      val bread = Right(Map("id" -> 3, "name" -> "bread", "price" -> 250))
      val yogurt = Right(Map("id" -> 4, "name" -> "yogurt", "price" -> 199))

      val subject = DatabaseExtractor(ConfigFactory.parseString(config), pc)

      subject.extract().futureValue should contain allElementsOf Seq(steak, broth, bread, yogurt)
    }

  }

}
