package it.ldsoftware.starling.engine.consumers

import com.typesafe.config.Config
import it.ldsoftware.starling.engine._
import it.ldsoftware.starling.extensions.DatabaseExtensions
import it.ldsoftware.starling.extensions.Interpolator.ExtendedConnection
import it.ldsoftware.starling.extensions.UsableExtensions.{LetOperations, UsableCloseable}

import javax.sql.DataSource
import scala.concurrent.{ExecutionContext, Future}

class DatabaseConsumer(query: String, ds: DataSource)(implicit val ec: ExecutionContext) extends Consumer {

  override def consumeSuccess(data: Extracted): Future[ConsumerResult] =
    Future {
      ds.getConnection.use { it =>
        it.prepareNamedStatement(query, data)
          .let(_.executeUpdate())
          .let(u => Consumed(s"DatabaseConsumer - $u rows affected by $query with values $data"))
      }
    }

}

object DatabaseConsumer extends ConsumerBuilder {

  override def apply(config: Config, pc: ProcessContext): DatabaseConsumer = {
    val query = config.getString("query")
    val conn = DatabaseExtensions.getDataSource(config)
    implicit val executionContext: ExecutionContext = pc.executionContext
    new DatabaseConsumer(query, conn)
  }

}
