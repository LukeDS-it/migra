package it.ldsoftware.starling.workers.model

/**
  * Result of the consume operation
  */
sealed trait ConsumerResult

/**
  * Used when the consume operation was successful
  * @param consumer the name of the consumer, for logging purposes
  */
case class Consumed(consumer: String) extends ConsumerResult

/**
  * Used when the consume operation was not successful
  * @param consumer the name of the consumer, for logging purposes
  * @param reason the reason why the data was not consumed
  * @param data if available, it contains the extracted data
  * @param err if available, it contains the throwable that made the operation fail
  */
case class NotConsumed(
    consumer: String,
    reason: String,
    data: Option[Extracted],
    err: Option[Throwable]
) extends ConsumerResult
