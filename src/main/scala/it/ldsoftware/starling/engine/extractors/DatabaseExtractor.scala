package it.ldsoftware.starling.engine.extractors
import com.typesafe.config.Config
import it.ldsoftware.starling.engine._
import it.ldsoftware.starling.engine.util.Interpolator._
import it.ldsoftware.starling.engine.util.ReflectionFactory
import slick.jdbc.JdbcBackend._
import slick.jdbc._

import scala.concurrent.Future

//noinspection SqlDialectInspection,SqlNoDataSourceInspection
class DatabaseExtractor(query: String, db: Database, profile: JdbcProfile) extends Extractor {

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

  override def apply(config: Config): Extractor = {
    val (query, db, profile) = ReflectionFactory.getDbInfo(config)
    new DatabaseExtractor(query, db, profile)
  }

  implicit val MapGetResult: GetResult[Map[String, Object]] = GetResult { r =>
    val meta = r.rs.getMetaData
    (1 to r.numColumns).map(i => meta.getColumnName(i).toLowerCase -> r.nextObject()).toMap
  }

}
