package it.ldsoftware.starling.server

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.starling.configuration.AppConfig
import it.ldsoftware.starling.server.http.{HealthRoutes, ProcessRoutes}
import it.ldsoftware.starling.server.persistence.Process
import it.ldsoftware.starling.server.services.ProcessService

import scala.util.{Failure, Success}

object ServerBehavior extends LazyLogging with Directives {

  private val AllInterfaces = "0.0.0.0"

  def apply(appConfig: AppConfig): Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      val system = context.system

      Process.init(system)

      val sharding = ClusterSharding(system)

      new Migrations(appConfig).migrate()

      val allRoutes = HealthRoutes() ~ ProcessRoutes(new ProcessService(sharding))
      startHttpServer(allRoutes, appConfig.serverPort)(system)

      Behaviors.empty
    }

  private def startHttpServer(routes: Route, port: Int)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    Http().newServerAt(AllInterfaces, port).bind(routes).onComplete {
      case Success(binding) =>
        logger.info(s"Studio Metrics ready on port ${binding.localAddress.getPort}")
      case Failure(exception) =>
        logger.error("Failed to bind HTTP endpoint, closing", exception)
        system.terminate()
    }
  }

}
