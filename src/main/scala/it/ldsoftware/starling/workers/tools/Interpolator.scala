package it.ldsoftware.starling.workers.tools

import freemarker.template.{Configuration, Template, Version}
import it.ldsoftware.starling.workers.model.Extracted
import slick.jdbc.{PositionedParameters, SQLActionBuilder}

import java.io.{StringReader, StringWriter}

object Interpolator {

  implicit class StringInterpolator(s: String) {
    private val template =
      new Template("tmp", new StringReader(s), new Configuration(new Version("2.3.31")))

    def interpolatedWith(data: Extracted): String = {
      val out = new StringWriter()
      template.process(data, out)
      val interpolated = out.toString
      out.flush()
      interpolated
    }
  }

  implicit class MapInterpolator(map: Map[String, Any]) {
    def interpolatedWith(data: Extracted): Map[String, Any] =
      map.view.mapValues {
        case x: String => x.interpolatedWith(data)
        case x         => x
      }.toMap
  }

  implicit class SlickInterpolator(a: SQLActionBuilder) {
    def + (b: SQLActionBuilder): SQLActionBuilder = {
      SQLActionBuilder(a.queryParts ++ b.queryParts, (p: Unit, pp: PositionedParameters) => {
        a.unitPConv.apply(p, pp)
        b.unitPConv.apply(p, pp)
      })
    }
  }

}
