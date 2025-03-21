package it.ldsoftware.migra.engine.consumers

import org.apache.pekko.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.ldsoftware.migra.DatabaseUtils
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.{Consumed, FileResolver, ProcessContext}
import org.apache.commons.lang3.RandomStringUtils
import org.mockito.IdiomaticMockito
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration.*

//noinspection SqlDialectInspection,SqlNoDataSourceInspection
class DatabaseConsumerIntSpec
    extends AnyWordSpec
    with Matchers
    with Eventually
    with ScalaFutures
    with IntegrationPatience
    with IdiomaticMockito {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(15, Millis)))

  import slick.jdbc.H2Profile.api.*

  private val pc = ProcessContext(ActorSystem("test"), mock[AppConfig], mock[FileResolver])
  private val jdbcUrl = s"jdbc:h2:mem:${RandomStringUtils.secure().nextAlphanumeric(10)};DB_CLOSE_DELAY=-1"
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
    "run a query to update a table after some extracted data" in {
      // language=JSON
      val config =
        s"""
           |{
           |  "query": "update products set price = :newPrice where name = :targetProduct",
           |  "jdbc-url": "$jdbcUrl",
           |  "jdbc-driver": "org.h2.Driver"
           |}
           |""".stripMargin

      val expectedPrice = 50L

      val subject = DatabaseConsumer(ConfigFactory.parseString(config), pc)

      val extracted = Map("newPrice" -> expectedPrice, "targetProduct" -> "steak")

      subject.consumeSuccess(extracted).futureValue shouldBe Consumed(
        s"DatabaseConsumer - 1 rows affected by update products set price = :newPrice where name = :targetProduct with values Map(newPrice -> $expectedPrice, targetProduct -> steak)"
      )

      eventually {
        db.run(sql"select price from products where name = 'steak'".as[Long]).futureValue shouldBe Vector(expectedPrice)
      }
    }
  }

}
