package it.ldsoftware.starling.engine

import akka.stream.Materializer
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}

/**
  * A Consumer is a terminal operation in a process. It consumes data coming from upstream.
  * More than one consumer can be plugged into a process.
  */
trait Consumer {

  implicit val ec: ExecutionContext

  /**
    * Consumes the outcome of the extraction process. This function is called whenever
    * the value was successfully extracted.
    *
    * @param data the data extracted from the previous step
    * @return a [[Future]] containing the result of the consuming operation
    */
  def consumeSuccess(data: Extracted): Future[ConsumerResult]

  /**
    * Consumes the outcome of the extraction process. This function is called whenever
    * the value was not extracted due to any kind of failure. By default it will return a
    * [[NotConsumed]] with the reason coming from upstream. Can be overridden.
    *
    * @param msg the message coming from the extraction result
    * @return a [[Future]] containing a default [[NotConsumed]] instance
    */
  def consumeFailure(msg: String): Future[ConsumerResult] = Future {
    NotConsumed(this.getClass.getName, s"Error during upstream extraction: $msg", None, None)
  }

  final def consume(data: ExtractionResult): Future[ConsumerResult] = data match {
    case Left(value)  => consumeFailure(value)
    case Right(value) => consumeSuccess(value).recover {
      case exc => NotConsumed(this.getClass.getSimpleName, exc.getMessage, Some(value), Some(exc))
    }
  }

}

/**
  * This class acts as a factory for a consumer. If you want your consumers to be
  * taken from the process configuration you will need to extend this trait in a companion
  * object of the consumer you're implementing
  */
trait ConsumerBuilder {

  /**
    * Must return an instance of the consumer created with the parameters specified in the
    * configuration
    * @param config the configuration of the consumer
    * @return an instance of a consumer
    */
  def apply(config: Config, ec: ExecutionContext, mat: Materializer): Consumer

}
