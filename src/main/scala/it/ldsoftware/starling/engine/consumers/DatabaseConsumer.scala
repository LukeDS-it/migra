package it.ldsoftware.starling.engine.consumers

import com.typesafe.config.Config
import it.ldsoftware.starling.engine._
import it.ldsoftware.starling.extensions.DatabaseExtensions
import it.ldsoftware.starling.extensions.Interpolator.ExtendedConnection
import it.ldsoftware.starling.extensions.UsableExtensions.UsableConnection

import java.sql.Connection
import scala.concurrent.{ExecutionContext, Future}

class DatabaseConsumer(query: String, conn: Connection)(implicit val ec: ExecutionContext)
    extends Consumer {

  override def consumeSuccess(data: Extracted): Future[ConsumerResult] = conn.use { it =>
    Future(it.prepareNamedStatement(query, data))
      .map(_.executeUpdate())
      .map(u => Consumed(s"DatabaseConsumer - $u rows affected by: $query"))
  }

}

object DatabaseConsumer extends ConsumerBuilder {

  override def apply(config: Config, pc: ProcessContext): DatabaseConsumer = {
    val query = config.getString("query")
    val conn = DatabaseExtensions.getConnection(config)
    implicit val executionContext: ExecutionContext = pc.executionContext
    new DatabaseConsumer(query, conn)
  }

}
