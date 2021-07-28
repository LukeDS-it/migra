package it.ldsoftware.starling.workers.consumers

import it.ldsoftware.starling.workers.model.{ConsumerResult, Extracted, ExtractionResult, NotConsumed}

import scala.concurrent.{ExecutionContext, Future}

/**
  * A Consumer is a terminal operation in a process. It consumes data coming from upstream.
  * More than one consumer can be plugged into a process.
  */
trait Consumer {

  implicit val ec: ExecutionContext = ExecutionContext.global

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
    case Right(value) => consumeSuccess(value)
  }

}
