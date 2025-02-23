package it.ldsoftware.migra.server.http

import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.{Directives, Route}

class HealthRoutes extends Directives {

  def routes: Route = healthRoute

  private def healthRoute =
    path("health") {
      complete((StatusCodes.OK, "OK"))
    }

}

object HealthRoutes {
  def apply(): Route = new HealthRoutes().routes
}
