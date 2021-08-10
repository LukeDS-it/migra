package it.ldsoftware.starling.engine.extractors
import com.typesafe.config.Config
import it.ldsoftware.starling.engine._
import it.ldsoftware.starling.engine.extractors.DatabaseExtractor.{AutoQuery, QueryType}
import it.ldsoftware.starling.engine.util.Interpolator._
import slick.jdbc.JdbcBackend._
import slick.jdbc.SetParameter._
import slick.jdbc._

import java.sql.{Date, Time, Timestamp}
import scala.concurrent.Future
import scala.jdk.CollectionConverters.CollectionHasAsScala

//noinspection SqlDialectInspection,SqlNoDataSourceInspection
class DatabaseExtractor(qt: QueryType, db: Database, driver: JdbcProfile) extends Extractor {

  import driver.api._

  val query = qt match {
    case Left(customQuery) => queryFromString(customQuery)
    case Right(autoQuery)  => queryFromAutoQuery(autoQuery)
  }

  override def extract(): Future[Seq[ExtractionResult]] =
    db.run(query.as[Extracted])
      .map(ex => ex.map(Right(_)))
      .recover {
        case exc => Seq(Left(exc.toString))
      }

  override def toPipedExtractor(data: Extracted): Extractor =
    new DatabaseExtractor(interpolate(qt, data), db, driver)

  implicit val extractor: GetResult[Extracted] =
    GetResult { r =>
      val meta = r.rs.getMetaData
      (1 to r.numColumns).map(i => meta.getColumnName(i) -> r.nextObject()).toMap
    }

  private def queryFromString(string: String): SQLActionBuilder = ???

  implicit val setParameter: SetParameter[Any] = DatabaseExtractor.SetGenericParameter

  private def queryFromAutoQuery(query: AutoQuery): SQLActionBuilder =
    query._3
      .map {
        case (field, value) => sql"#$field = $value"
      }
      .foldLeft(sql"select #${query._2.mkString(",")} from #${query._1} where") {
        case (acc, next) => acc + next
      }

  private def interpolate(queryType: QueryType, data: Extracted): QueryType = queryType match {
    case Left(value) => Left(value <-- data)
    case Right(value) => Right((value._1, value._2, value._3 <-- data))
  }

}

object DatabaseExtractor extends ExtractorBuilder {

  type CustomQuery = String
  type Table = String
  type Select = List[String]
  type Where = Map[String, Any]
  type AutoQuery = (Table, Select, Where)

  type QueryType = Either[CustomQuery, AutoQuery]

  override def apply(config: Config): Extractor = {
    val queryType: QueryType = if (config.hasPath("fields")) {
      val table = config.getString("table")
      val fields = config.getStringList("fields").asScala.toList
      val where = config.getConfigSList("where").map { c =>
        val field = c.entrySet().asScala.head
        field.getKey -> c.getAnyRef(field.getKey).asInstanceOf[Any]
      }.toMap
      Right((table, fields, where))
    } else {
      Left(config.getString("query"))
    }

    val db = ???

    val driver = ???

    new DatabaseExtractor(queryType, db, driver)
  }

  implicit object SetGenericParameter extends SetParameter[Any] {
    override def apply(v1: Any, pp: PositionedParameters): Unit = v1 match {
      case v: BigDecimal => SetBigDecimal.apply(v, pp)
      case v: Boolean => SetBoolean.apply(v, pp)
      case v: Byte => SetByte.apply(v, pp)
      case v: Date => SetDate.apply(v, pp)
      case v: Double => SetDouble.apply(v, pp)
      case v: Float => SetFloat.apply(v, pp)
      case v: Int => SetInt.apply(v, pp)
      case v: Long => SetLong.apply(v, pp)
      case v: Short => SetShort.apply(v, pp)
      case v: String => SetString.apply(v, pp)
      case v: Time => SetTime.apply(v, pp)
      case v: Timestamp => SetTimestamp.apply(v, pp)
    }
  }
}
