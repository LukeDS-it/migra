package it.ldsoftware.migra.engine.extractors

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.migra.engine._
import it.ldsoftware.migra.extensions.ConfigExtensions.ConfigOperations
import it.ldsoftware.migra.extensions.Interpolator.StringInterpolator
import it.ldsoftware.migra.extensions.JacksonExtension._

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
      .flatMap(r => Unmarshal(r).to[String].map(s => (r.status, s)))
      .map {
        case (status, json) if status.intValue() < 300 => produceSuccess(json.jsonGet(subPath))
        case (_, error)                                => Seq(Left(error))
      }

  override def toPipedExtractor(data: Extracted): Extractor =
    new HttpExtractor(url <-- data, subPath, auth, http, config, data)

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
    implicit val executionContext: ExecutionContext = pc.executionContext
    implicit val materializer: Materializer = pc.materializer

    new HttpExtractor(url, subPath, auth, pc.http, config, Map())
  }

}
