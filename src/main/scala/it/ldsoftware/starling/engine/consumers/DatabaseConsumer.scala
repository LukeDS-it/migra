package it.ldsoftware.starling.engine.consumers

import com.typesafe.config.Config
import it.ldsoftware.starling.engine._
import it.ldsoftware.starling.engine.util.Interpolator.StringInterpolator
import it.ldsoftware.starling.engine.util.ReflectionFactory
import slick.jdbc.JdbcBackend._
import slick.jdbc._

import scala.concurrent.Future

//noinspection SqlDialectInspection,SqlNoDataSourceInspection
class DatabaseConsumer(query: String, db: Database, profile: JdbcProfile) extends Consumer {

  import profile.api._

  override def consumeSuccess(data: Extracted): Future[ConsumerResult] = {
    val update = query <-- data
    db.run(sqlu"#$update")
      .map(u => Consumed(s"DatabaseConsumer - $u rows affected by: $update"))
  }

}

object DatabaseConsumer extends ConsumerBuilder {

  override def apply(config: Config): DatabaseConsumer = {
    val (query, db, profile) = ReflectionFactory.getDbInfo(config)
    new DatabaseConsumer(query, db, profile)
  }

}
