package it.ldsoftware.starling.engine.extractors

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, OAuth2BearerToken}
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.starling.engine._
import it.ldsoftware.starling.engine.extractors.HttpExtractor.AuthMethod
import it.ldsoftware.starling.extensions.Interpolator.StringInterpolator
import it.ldsoftware.starling.extensions.ConfigExtensions.ConfigOperations
import it.ldsoftware.starling.extensions.CredentialManager
import it.ldsoftware.starling.extensions.JacksonExtension._

import scala.concurrent.{ExecutionContext, Future}

class HttpExtractor(
    url: String,
    subPath: Option[String],
    auth: AuthMethod,
    http: HttpExt,
    override val config: Config,
    override val initialValue: Extracted
)(implicit
    val ec: ExecutionContext,
    mat: Materializer
) extends Extractor {

  override def doExtract(): Future[Seq[ExtractionResult]] =
    auth.toHeaders
      .map(headers => HttpRequest(uri = url, headers = headers))
      .flatMap(r => http.singleRequest(r))
      .flatMap(r => Unmarshal(r).to[String])
      .map(json => json.jsonGet(subPath))
      .map {
        case SubArray(seq)   => seq.map(Right(_))
        case SubGeneric(gen) => Seq(Right(gen))
      }

  override def toPipedExtractor(data: Extracted): Extractor =
    new HttpExtractor(url <-- data, subPath, auth, http, config, data)

}

object HttpExtractor extends ExtractorBuilder {

  sealed trait AuthMethod {
    def toHeaders(implicit ec: ExecutionContext): Future[Seq[HttpHeader]] =
      this match {
        case NoAuth                => Future.successful(Seq())
        case BasicAuth(user, pass) => Future.successful(Seq(Authorization(BasicHttpCredentials(user, pass))))
        case BearerAuth(bearer)    => Future.successful(Seq(Authorization(OAuth2BearerToken(bearer))))
        case OAuth2Auth(cache)     => cache.token.map(t => Seq(Authorization(OAuth2BearerToken(t))))
      }
  }

  case object NoAuth extends AuthMethod
  case class BasicAuth(user: String, pass: String) extends AuthMethod
  case class BearerAuth(bearer: String) extends AuthMethod
  case class OAuth2Auth(tokenCache: TokenProvider) extends AuthMethod

  override def apply(config: Config, pc: ProcessContext): Extractor = {
    val url = config.getString("url")
    val subPath = config.getOptString("subPath")
    val auth = extractAuth(config, pc)
    implicit val executionContext: ExecutionContext = pc.executionContext
    implicit val materializer: Materializer = pc.materializer

    new HttpExtractor(url, subPath, auth, pc.http, config, Map())
  }

  private def extractAuth(config: Config, pc: ProcessContext): AuthMethod = {
    if (config.hasPath("auth")) {
      val authConfig = config.getConfig("auth")
      authConfig.getString("type") match {
        case "basic"  => BasicAuth.tupled(CredentialManager.getCredentials(authConfig))
        case "bearer" => BearerAuth(CredentialManager.getToken(authConfig))
        case "oauth2" => OAuth2Auth(pc.getTokenCache(authConfig.getString("provider")))
      }
    } else {
      NoAuth
    }
  }

}
