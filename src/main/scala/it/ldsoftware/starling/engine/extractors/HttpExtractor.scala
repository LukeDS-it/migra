package it.ldsoftware.starling.engine.extractors

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import it.ldsoftware.starling.engine.extractors.HttpExtractor.AuthMehtod
import it.ldsoftware.starling.engine.{Extracted, ExtractionResult, Extractor, ExtractorBuilder}
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class HttpExtractor(url: String, auth: AuthMehtod, http: HttpExt)(implicit val ec: ExecutionContext, mat: Materializer)
    extends Extractor
    with FailFastCirceSupport {

  override def extract(): Future[Seq[ExtractionResult]] =
    Future(auth.toHeaders)
      .map(headers => HttpRequest(uri = url, headers = headers))
      .flatMap(r => http.singleRequest(r))
      .flatMap(r => Unmarshal(r).to[Json])
      .map(???)
//      .map(json => json.asObject)

  override def toPipedExtractor(data: Extracted): Extractor = ???

}

object HttpExtractor extends ExtractorBuilder {

  sealed trait AuthMehtod {
    def toHeaders: Seq[HttpHeader] =
      this match {
        case None => Seq()
      }
  }

  case object None extends AuthMehtod

  override def apply(config: Config, ec: ExecutionContext, mat: Materializer): Extractor = ???
}
