package it.ldsoftware.starling.engine.extractors
import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.starling.engine._
import it.ldsoftware.starling.engine.util.Interpolator._
import it.ldsoftware.starling.engine.util.ReflectionFactory
import slick.jdbc.JdbcBackend._
import slick.jdbc._

import scala.concurrent.{ExecutionContext, Future}

//noinspection SqlDialectInspection,SqlNoDataSourceInspection
class DatabaseExtractor(query: String, db: Database, profile: JdbcProfile)(implicit val ec: ExecutionContext)
    extends Extractor {

  import DatabaseExtractor.MapGetResult
  import profile.api._

  override def extract(): Future[Seq[ExtractionResult]] =
    db.run(sql"#$query".as[Extracted])
      .map(ex => ex.map(Right(_)))
      .recover {
        case exc => Seq(Left(exc.toString))
      }

  override def toPipedExtractor(data: Extracted): Extractor =
    new DatabaseExtractor(query <-- data, db, profile)

}

object DatabaseExtractor extends ExtractorBuilder {

  override def apply(config: Config, ec: ExecutionContext, mat: Materializer): Extractor = {
    val (query, db, profile) = ReflectionFactory.getDbInfo(config)
    implicit val executionContext: ExecutionContext = ec
    new DatabaseExtractor(query, db, profile)
  }

  implicit val MapGetResult: GetResult[Map[String, Object]] = GetResult { r =>
    val meta = r.rs.getMetaData
    (1 to r.numColumns).map(i => meta.getColumnName(i).toLowerCase -> r.nextObject()).toMap
  }

}
