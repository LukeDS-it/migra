package it.ldsoftware.migra.engine.providers

import com.typesafe.config.Config
import it.ldsoftware.migra.engine.{ProcessContext, TokenProvider, TokenProviderBuilder}

import scala.concurrent.Future

class DummyTokenProvider(val name: String, fixedToken: String) extends TokenProvider {
  override def token: Future[String] = Future.successful(fixedToken)
}

object DummyTokenProvider extends TokenProviderBuilder {
  override def apply(name: String, config: Config, pc: ProcessContext): TokenProvider =
    new DummyTokenProvider(name, config.getString("output"))
}
