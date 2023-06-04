package it.ldsoftware.migra.engine.extractors

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.migra.engine._
import it.ldsoftware.migra.extensions.ConfigExtensions.ConfigOperations
import it.ldsoftware.migra.extensions.Interpolator.StringInterpolator
import it.ldsoftware.migra.extensions.JacksonExtension._

import scala.concurrent.{ExecutionContext, Future}

class HttpExtractor(
    httpMethod: HttpMethod = HttpMethods.GET,
    url: String,
    subPath: Option[String],
    auth: AuthMethod,
    requestBody: Option[String] = None,
    http: HttpExt,
    override val config: Config,
    override val initialValue: Extracted
)(implicit
    val ec: ExecutionContext,
    mat: Materializer
) extends Extractor {

  override def doExtract(): Future[Seq[ExtractionResult]] =
    auth.toHeaders
      .map(headers => makeRequest(httpMethod, url, headers, requestBody))
      .flatMap(r => http.singleRequest(r))
      .flatMap(r => Unmarshal(r).to[String].map(s => (r.status, s)))
      .map {
        case (status, json) if status.intValue() < 300 => produceSuccess(json.jsonGet(subPath))
        case (status, error)                           => Seq(Left(s"Unexpected response: Status $status - $error"))
      }

  private def makeRequest(method: HttpMethod, url: String, headers: Seq[HttpHeader], body: Option[String]) =
    body
      .map(b =>
        HttpRequest(
          method = method,
          uri = url,
          headers = headers,
          entity = HttpEntity(MediaTypes.`application/json`, b)
        )
      )
      .getOrElse(HttpRequest(method = method, uri = url, headers = headers))

  override def toPipedExtractor(data: Extracted): Extractor =
    new HttpExtractor(httpMethod, url <-- data, subPath, auth, requestBody.map(_ <-- data), http, config, data)

  override def summary: String = "HttpExtractor failed TODO"

  private def produceSuccess(property: SubProperty): Seq[ExtractionResult] =
    property match {
      case SubArray(seq)   => seq.map(e => Right(e))
      case SubGeneric(gen) => Seq(Right(gen))
    }
}

object HttpExtractor extends ExtractorBuilder {

  override def apply(config: Config, pc: ProcessContext): Extractor = {
    val url = config.getString("url")
    val subPath = config.getOptString("subPath")
    val auth = AuthMethod.fromConfig(config, pc)

    val method = config.getOptString("method").flatMap(HttpMethods.getForKeyCaseInsensitive).getOrElse(HttpMethods.GET)
    val jsonBody =
      config.getOptConfig("body").map(_.toString).map(s => s.substring(s.indexOf("{"), s.lastIndexOf("}") + 1))

    implicit val executionContext: ExecutionContext = pc.executionContext
    implicit val materializer: Materializer = pc.materializer

    new HttpExtractor(method, url, subPath, auth, jsonBody, pc.http, config, Map())
  }

}
