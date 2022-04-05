package it.ldsoftware.starling.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}

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
