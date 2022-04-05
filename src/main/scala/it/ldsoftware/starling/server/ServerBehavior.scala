package it.ldsoftware.starling.server

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.starling.configuration.AppConfig
import it.ldsoftware.starling.http.HealthRoutes
import it.ldsoftware.starling.persistence.Process

import scala.util.{Failure, Success}

object ServerBehavior extends LazyLogging {

  private val AllInterfaces = "0.0.0.0"

  def apply(appConfig: AppConfig): Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      val system = context.system

      Process.init(system)

      new Migrations(appConfig).migrate()

      val allRoutes = HealthRoutes()
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
