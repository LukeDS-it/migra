package it.ldsoftware.migra.engine

import com.typesafe.config.Config

import scala.concurrent.Future

trait TokenProvider {

  val name: String

  def token: Future[String]

}

trait TokenProviderBuilder {
  def apply(alias: String, config: Config, pc: ProcessContext): TokenProvider
}
