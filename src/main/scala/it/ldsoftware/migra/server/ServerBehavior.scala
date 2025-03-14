package it.ldsoftware.migra.server

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.server.http.{HealthRoutes, ProcessRoutes}
import it.ldsoftware.migra.server.persistence.Process
import it.ldsoftware.migra.server.services.ProcessService

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

  private def startHttpServer(routes: Route, port: Int)(implicit system: ActorSystem[?]): Unit = {
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
