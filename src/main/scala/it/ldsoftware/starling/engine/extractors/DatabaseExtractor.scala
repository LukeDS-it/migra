package it.ldsoftware.starling.engine.extractors
import com.typesafe.config.Config
import it.ldsoftware.starling.engine._
import it.ldsoftware.starling.engine.util.Interpolator._
import slick.jdbc.JdbcBackend._
import slick.jdbc.SetParameter._
import slick.jdbc._

import java.sql.{Date, Time, Timestamp}
import scala.concurrent.Future

//noinspection SqlDialectInspection,SqlNoDataSourceInspection
class DatabaseExtractor(query: String, db: Database, profile: JdbcProfile) extends Extractor {

  import profile.api._
  import DatabaseExtractor.MapGetResult

  implicit val setParameter: SetParameter[Any] = DatabaseExtractor.SetGenericParameter

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
    val query = config.getString("query")
    val jdbcUrl = config.getString("jdbc-url")
    val jdbcDriver = config.getString("jdbc-driver")

    val db = Database.forURL(jdbcUrl, driver = jdbcDriver)
    val profile = getProfile(jdbcDriver)

    new DatabaseExtractor(query, db, profile)
  }

  private def getProfile(jdbcDriver: String): JdbcProfile =
    jdbcDriver match {
      case "org.apache.derby.jdbc.EmbeddedDriver" => DerbyProfile
      case "org.h2.Driver"                        => H2Profile
      case "org.hsqldb.jdbcDriver"                => HsqldbProfile
      case "com.mysql.jdbc.Driver"                => MySQLProfile
      case "org.postgresql.Driver"                => PostgresProfile
      case "org.sqlite.JDBC"                      => SQLiteProfile
    }

  implicit object SetGenericParameter extends SetParameter[Any] {
    override def apply(v1: Any, pp: PositionedParameters): Unit =
      v1 match {
        case v: BigDecimal => SetBigDecimal.apply(v, pp)
        case v: Boolean    => SetBoolean.apply(v, pp)
        case v: Byte       => SetByte.apply(v, pp)
        case v: Date       => SetDate.apply(v, pp)
        case v: Double     => SetDouble.apply(v, pp)
        case v: Float      => SetFloat.apply(v, pp)
        case v: Int        => SetInt.apply(v, pp)
        case v: Long       => SetLong.apply(v, pp)
        case v: Short      => SetShort.apply(v, pp)
        case v: String     => SetString.apply(v, pp)
        case v: Time       => SetTime.apply(v, pp)
        case v: Timestamp  => SetTimestamp.apply(v, pp)
      }
  }

  implicit val MapGetResult: GetResult[Map[String, Object]] = GetResult { r =>
    val meta = r.rs.getMetaData
    (1 to r.numColumns).map(i => meta.getColumnName(i).toLowerCase -> r.nextObject()).toMap
  }

}
