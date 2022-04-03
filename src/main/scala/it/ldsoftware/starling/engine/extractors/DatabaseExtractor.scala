package it.ldsoftware.starling.engine.extractors
import com.typesafe.config.Config
import it.ldsoftware.starling.engine._
import it.ldsoftware.starling.engine.extractors.DatabaseExtractor.resultSetToList
import it.ldsoftware.starling.extensions.Interpolator._
import it.ldsoftware.starling.extensions.DatabaseExtensions
import it.ldsoftware.starling.extensions.UsableExtensions.{LetOperations, UsableConnection}
import slick.jdbc._

import java.sql.{Connection, ResultSet}
import scala.concurrent.{ExecutionContext, Future}

class DatabaseExtractor(query: String, conn: Connection, params: Extracted = Map())(implicit
    val ec: ExecutionContext
) extends Extractor {

  override def extract(): Future[Seq[ExtractionResult]] =
    Future {
      conn.use { it =>
        it.prepareNamedStatement(query, params)
          .let(_.executeQuery())
          .let(resultSetToList)
          .let(_.map(Right(_)))
      }
    }.recover {
      case exc => Seq(Left(exc.toString))
    }

  override def toPipedExtractor(data: Extracted): Extractor =
    new DatabaseExtractor(query, conn, data)

}

object DatabaseExtractor extends ExtractorBuilder {

  override def apply(config: Config, pc: ProcessContext): Extractor = {
    val query = config.getString("query")
    val conn = DatabaseExtensions.getConnection(config)
    implicit val executionContext: ExecutionContext = pc.executionContext
    new DatabaseExtractor(query, conn)
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
