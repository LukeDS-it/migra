package it.ldsoftware.starling.engine.providers

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{FormData, HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import it.ldsoftware.starling.engine.providers.OAuth2TokenProvider.{Credentials, OAuth2Authentication, TimedToken}
import it.ldsoftware.starling.engine.{ProcessContext, TokenProvider, TokenProviderBuilder}

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class OAuth2TokenProvider(val name: String, endpoint: String, credentials: Credentials, http: HttpExt)(implicit
    val ec: ExecutionContext,
    mat: Materializer
) extends TokenProvider
    with FailFastCirceSupport {

  private var intToken: Option[TimedToken] = None

  override def token: Future[String] =
    intToken
      .filter(_.notExpired)
      .map(_.token)
      .map(Future.successful)
      .getOrElse(updateAndGet())

  private def updateAndGet(): Future[String] =
    Future
      .successful(credentials)
      .map { c =>
        FormData(
          Map(
            "grant_type" -> "client_credentials",
            "client_id" -> c.id,
            "client_secret" -> c.secret
          )
        )
      }
      .map(e => HttpRequest(uri = endpoint, method = HttpMethods.POST, entity = e.toEntity))
      .flatMap(r => http.singleRequest(r))
      .flatMap(resp => Unmarshal(resp).to[OAuth2Authentication])
      .map { auth =>
        val newToken = TimedToken(auth.access_token, Instant.now().plusMillis(auth.expires_in))
        intToken = Some(newToken)
        newToken.token
      }
}

object OAuth2TokenProvider extends TokenProviderBuilder {
  override def apply(alias: String, config: Config, pc: ProcessContext): TokenProvider =
    new OAuth2TokenProvider(
      alias,
      config.getString("endpoint"),
      Credentials(config.getString("id"), config.getString("secret")),
      pc.http
    )(pc.executionContext, pc.materializer)

  case class TimedToken(token: String, expiresAt: Instant) {
    def notExpired: Boolean = Instant.now().plusSeconds(10).isBefore(expiresAt)
  }

  case class Credentials(id: String, secret: String)

  case class OAuth2Authentication(access_token: String, expires_in: Long)
}
