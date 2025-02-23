package it.ldsoftware.migra.engine.extractors
import com.typesafe.config.Config
import it.ldsoftware.migra.engine.*
import it.ldsoftware.migra.engine.extractors.DatabaseExtractor.resultSetToList
import it.ldsoftware.migra.extensions.DatabaseExtensions
import it.ldsoftware.migra.extensions.Interpolator.*
import it.ldsoftware.migra.extensions.UsableExtensions.{LetOperations, UsableCloseable}
import slick.jdbc.*

import java.sql.ResultSet
import javax.sql.DataSource
import scala.concurrent.{ExecutionContext, Future}

class DatabaseExtractor(
    query: String,
    ds: DataSource,
    override val config: Config,
    override val initialValue: Extracted = Map()
)(implicit
    val ec: ExecutionContext
) extends Extractor {

  override def doExtract(): Future[Seq[ExtractionResult]] =
    Future {
      ds.getConnection.use { it =>
        it.prepareNamedStatement(query, initialValue)
          .let(_.executeQuery())
          .let(resultSetToList)
          .let(_.map(Right(_)))
      }
    }

  override def toPipedExtractor(data: Extracted): Extractor =
    new DatabaseExtractor(query, ds, config, data)

  override def summary: String =
    s"Database extractor trying to execute $query interpolated with $initialValue"

}

object DatabaseExtractor extends ExtractorBuilder {

  override def apply(config: Config, pc: ProcessContext): Extractor = {
    val query = config.getString("query")
    val conn = DatabaseExtensions.getDataSource(config)
    implicit val executionContext: ExecutionContext = pc.executionContext
    new DatabaseExtractor(query, conn, config)
  }

  def resultSetToList(rs: ResultSet): Seq[Extracted] =
    rs.getMetaData.let { meta =>
      new Iterator[Extracted] {
        def hasNext: Boolean = rs.next()

        def next(): Extracted =
          (1 to meta.getColumnCount).map(i => meta.getColumnName(i).toLowerCase -> rs.getObject(i)).toMap
      }
    }.toList

  implicit val MapGetResult: GetResult[Map[String, Object]] = GetResult { r =>
    val meta = r.rs.getMetaData
    (1 to r.numColumns).map(i => meta.getColumnName(i).toLowerCase -> r.nextObject()).toMap
  }

}
