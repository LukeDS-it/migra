package it.ldsoftware.starling.engine.util

import freemarker.template.{Configuration, Template, Version}
import it.ldsoftware.starling.engine.Extracted
import slick.jdbc.{PositionedParameters, SQLActionBuilder}

import java.io.{StringReader, StringWriter}
import scala.jdk.CollectionConverters._

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

  implicit class SlickInterpolator(a: SQLActionBuilder) {
    def +(b: SQLActionBuilder): SQLActionBuilder = {
      SQLActionBuilder(
        a.queryParts ++ b.queryParts,
        (p: Unit, pp: PositionedParameters) => {
          a.unitPConv.apply(p, pp)
          b.unitPConv.apply(p, pp)
        }
      )
    }
  }

}
