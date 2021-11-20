package it.ldsoftware.starling.engine.extractors

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.starling.engine._
import it.ldsoftware.starling.engine.extractors.HttpExtractor.AuthMehtod
import it.ldsoftware.starling.engine.util.Interpolator.StringInterpolator
import it.ldsoftware.starling.extensions.ConfigExtensions.ConfigOperations
import it.ldsoftware.starling.extensions.JacksonExtension._

import scala.concurrent.{ExecutionContext, Future}

class HttpExtractor(url: String, subPath: Option[String], auth: AuthMehtod, http: HttpExt)(implicit
    val ec: ExecutionContext,
    mat: Materializer
) extends Extractor {

  override def extract(): Future[Seq[ExtractionResult]] =
    Future(auth.toHeaders)
      .map(headers => HttpRequest(uri = url, headers = headers))
      .flatMap(r => http.singleRequest(r))
      .flatMap(r => Unmarshal(r).to[String])
      .map(json => json.jsonGet(subPath))
      .map {
        case SubArray(seq) => seq.map(Right(_))
        case SubGeneric(gen) => Seq(Right(gen))
      }

  override def toPipedExtractor(data: Extracted): Extractor =
    new HttpExtractor(url <-- data, subPath, auth, http)

}

object HttpExtractor extends ExtractorBuilder {

  sealed trait AuthMehtod {
    def toHeaders: Seq[HttpHeader] =
      this match {
        case NoAuth => Seq()
      }
  }

  case object NoAuth extends AuthMehtod

  override def apply(config: Config, pc: ProcessContext): Extractor = {
    val url = config.getString("url")
    val subPath = config.getOptString("subPath")
    val auth = NoAuth
    implicit val executionContext: ExecutionContext = pc.executionContext
    implicit val materializer: Materializer = pc.materializer

    new HttpExtractor(url, subPath, auth, pc.http)
  }
}
