package it.ldsoftware.migra.engine.consumers

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.*
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.Materializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.migra.engine.*
import it.ldsoftware.migra.extensions.ConfigExtensions.ConfigOperations
import it.ldsoftware.migra.extensions.Interpolator.*

import scala.concurrent.{ExecutionContext, Future}

class HttpJsonConsumer(
    url: String,
    jsonTemplate: String,
    method: HttpMethod,
    auth: AuthMethod,
    http: HttpExt,
    val config: Config
)(implicit
    val ec: ExecutionContext,
    mat: Materializer
) extends Consumer
    with LazyLogging {

  override def consumeSuccess(data: Extracted): Future[ConsumerResult] =
    Future(jsonTemplate <-- data)
      .flatMap(json => sendRequest(json, data))
      .recover(exc => NotConsumed(getClass.getName, "An exception occurred", Some(data), Some(exc)))

  private def sendRequest(json: String, data: Extracted): Future[ConsumerResult] =
    Future(json)
      .map(json => HttpEntity(MediaTypes.`application/json`, json))
      .zip(auth.toHeaders)
      .map { case (entity, headers) =>
        HttpRequest(method, url <-- data, headers, entity)
      }
      .flatMap(r => http.singleRequest(r))
      .flatMap(r => Unmarshal(r).to[String].map(s => (r.status, s)))
      .map { case (code, body) =>
        if (code.isSuccess())
          Consumed(s"${method.value} ${url <-- data} with $json executed with success")
        else
          NotConsumed(getClass.getName, s"Calling $url with $json returned $code: $body", Some(data), None)
      }
}

object HttpJsonConsumer extends ConsumerBuilder {

  override def apply(config: Config, pc: ProcessContext): Consumer = {
    val url = config.getString("url")
    val auth = AuthMethod.fromConfig(config, pc)
    val template = config.getString("template")
    val method = config.getOptString("method") match {
      case Some(name) => HttpMethods.getForKeyCaseInsensitive(name).getOrElse(HttpMethods.POST)
      case None       => HttpMethods.POST
    }
    implicit val executionContext: ExecutionContext = pc.executionContext
    implicit val materializer: Materializer = pc.materializer

    new HttpJsonConsumer(url, template, method, auth, pc.http, config)
  }

}
