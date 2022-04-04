package it.ldsoftware.starling

import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.configuration.AppConfig
import it.ldsoftware.starling.extensions.UsableExtensions.UsableSource
import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source

//noinspection SqlDialectInspection,SqlNoDataSourceInspection
class StarlingStandaloneSpec
    extends AnyFeatureSpec
    with GivenWhenThen
    with Matchers
    with ScalaFutures
    with Eventually
    with IntegrationPatience {

  import slick.jdbc.H2Profile.api._

  private val jdbcUrl = "jdbc:h2:mem:integration;DB_CLOSE_ON_EXIT=FALSE"
  val db = DatabaseUtils.getDatabase(jdbcUrl, "org.h2.Driver")

  private val dbSetup = db.run(
    DBIO.seq(
      sqlu"create table products (id int not null, name varchar (255) not null, price bigint not null)",
      sqlu"insert into products values (1, 'steak', 1000)",
      sqlu"insert into products values (2, 'broth', 1250)",
      sqlu"insert into products values (3, 'bread', 250)",
      sqlu"insert into products values (4, 'yogurt', 199)",
      sqlu"create table bought(id int not null auto_increment, name varchar(255))"
    )
  )

  Await.ready(dbSetup, 2.seconds)

  Feature("The application is able to run standalone processes") {
    Scenario("We want to move data from one table to another") {
      Given("a test process")
      val descriptor = "src/it/resources/test-db-to-db.json"
      val descriptorPath = new File(descriptor).getAbsolutePath
      And("a configuration for standalone operations")
      val standalone = "standalone-mode.conf"
      val appConfig = AppConfig(ConfigFactory.parseResources(standalone))

      When("the application sets up")
      StarlingApp.run(appConfig, Array(descriptorPath))

      Then("the application will take data from one table and put it into another")
      eventually {
        db.run(sql"select name from bought".as[String])
          .futureValue should contain allElementsOf Seq("steak", "bread", "yogurt")
      }

      And("a report will be generated")
      eventually {
        val generated = new File("src/it/resources/")
          .listFiles()
          .map(_.getAbsolutePath)
          .filter(_.contains("executed"))
        generated should have size 1

        val log = Source.fromFile(generated(0)).use(_.getLines().toList)

        log should contain allElementsOf Seq(
          """DatabaseConsumer - 1 rows affected by insert into bought (name) values(:name) with values Map(name -> steak)""",
          """DatabaseConsumer - 1 rows affected by insert into bought (name) values(:name) with values Map(name -> bread)""",
          """DatabaseConsumer - 1 rows affected by insert into bought (name) values(:name) with values Map(name -> yogurt)"""
        )

      }

      new File("src/it/resources/")
        .listFiles()
        .filter(_.getAbsolutePath.contains("executed"))
        .foreach(_.delete())
    }
  }

}
