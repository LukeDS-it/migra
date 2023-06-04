package it.ldsoftware.migra.engine

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, GenericHttpCredentials, OAuth2BearerToken}
import com.typesafe.config.Config
import it.ldsoftware.migra.extensions.CredentialManager

import scala.concurrent.{ExecutionContext, Future}

sealed trait AuthMethod {
  def toHeaders(implicit ec: ExecutionContext): Future[Seq[HttpHeader]] =
    this match {
      case NoAuth                    => Future.successful(Seq())
      case BasicAuth(user, pass)     => Future.successful(Seq(Authorization(BasicHttpCredentials(user, pass))))
      case BearerAuth(bearer)        => Future.successful(Seq(Authorization(OAuth2BearerToken(bearer))))
      case OAuth2Auth(cache)         => cache.token.map(t => Seq(Authorization(OAuth2BearerToken(t))))
      case CustomAuth(scheme, token) => Future.successful(Seq(Authorization(GenericHttpCredentials(scheme, token))))
    }
}

case object NoAuth extends AuthMethod
case class BasicAuth(user: String, pass: String) extends AuthMethod
case class BearerAuth(bearer: String) extends AuthMethod
case class OAuth2Auth(tokenCache: TokenProvider) extends AuthMethod
case class CustomAuth(scheme: String, token: String) extends AuthMethod

object AuthMethod {

  def fromConfig(config: Config, pc: ProcessContext): AuthMethod =
    if (config.hasPath("auth")) {
      val authConfig = config.getConfig("auth")
      authConfig.getString("type") match {
        case "basic"  => BasicAuth.tupled(CredentialManager.getCredentials(authConfig))
        case "bearer" => BearerAuth(CredentialManager.getToken(authConfig))
        case "oauth2" => OAuth2Auth(pc.getTokenCache(authConfig.getString("provider")))
        case "custom" => CustomAuth(authConfig.getString("schema"), authConfig.getString("token"))
      }
    } else {
      NoAuth
    }

}
