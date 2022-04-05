package it.ldsoftware.starling.server.http

import akka.http.scaladsl.server.Route
import it.ldsoftware.starling.server.services.ProcessService

class ProcessRoutes(processService: ProcessService) {
  def routes: Route = ???
}

object ProcessRoutes {
  def apply(processService: ProcessService): Route = new ProcessRoutes(processService).routes
}
