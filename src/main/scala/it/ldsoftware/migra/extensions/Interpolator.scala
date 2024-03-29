package it.ldsoftware.migra.extensions

import freemarker.template.{Configuration, Template, Version}
import it.ldsoftware.migra.engine.Extracted
import it.ldsoftware.migra.extensions.UsableExtensions.MutateOperations

import java.io.{StringReader, StringWriter}
import java.sql.{Connection, PreparedStatement}
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.MapHasAsJava

object Interpolator {

  implicit class StringInterpolator(s: String) {
    private val template =
      new Template("tmp", new StringReader(s), new Configuration(new Version("2.3.31")))

    def <--(data: Extracted): String = interpolatedWith(data)

    def interpolatedWith(data: Extracted): String = {
      val out = new StringWriter()
      template.process(data.asJava, out)
      val interpolated = out.toString
      out.flush()
      interpolated
    }
  }

  implicit class MapInterpolator(map: Map[String, Any]) {

    def <--(data: Extracted): Map[String, Any] = interpolatedWith(data)

    def interpolatedWith(data: Extracted): Map[String, Any] =
      map.view.mapValues {
        case x: String => x <-- data
        case x         => x
      }.toMap
  }

  implicit class ExtendedConnection(connection: Connection) {

    private val Param = """:([\w\d]*)""".r.unanchored

    private[extensions] def getPositionalQuery(query: String): (String, Map[String, Int]) = {

      @tailrec def replaceNext(query: String, acc: Seq[String]): (String, Map[String, Int]) =
        if (Param.matches(query))
          replaceNext(query.replaceFirst(Param.regex, "?"), acc :+ Param.findFirstMatchIn(query).get.group(1))
        else
          (query, acc.zipWithIndex.map(t => t._1 -> (t._2 + 1)).toMap)

      replaceNext(query, Seq())
    }

    def prepareNamedStatement(query: String, params: Extracted): PreparedStatement =
      getPositionalQuery(query) match {
        case (positional, paramMap) =>
          paramMap.foldLeft(connection.prepareStatement(positional)) { case (ps, next) =>
            ps.mutate(_.setObject(next._2, params(next._1)))
          }
      }
  }

}
