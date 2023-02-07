package it.ldsoftware.migra

package object engine {

  /** The data that flows through the process
    */
  type Extracted = Map[String, Any]

  /** The result of an extraction. Contains
    *
    *   - a String in Left if some error happened during the process
    *   - an Extracted in Right with the real data if the operation was successful
    */
  type ExtractionResult = Either[String, Extracted]

  /** Result of the consume operation
    */
  sealed trait ConsumerResult

  /** Used when the consume operation was successful
    * @param info
    *   any info that will be logged if the consumer succeeds
    */
  case class Consumed(info: String) extends ConsumerResult

  /** Used when the consume operation was not successful
    * @param consumer
    *   the name of the consumer, for logging purposes
    * @param reason
    *   the reason why the data was not consumed
    * @param data
    *   if available, it contains the extracted data
    * @param err
    *   if available, it contains the throwable that made the operation fail
    */
  case class NotConsumed(
      consumer: String,
      reason: String,
      data: Option[Extracted],
      err: Option[Throwable]
  ) extends ConsumerResult

  def getBuilder[T](fullyQualifiedClassName: String): T = {
    val mirror = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)
    val module = mirror.staticModule(fullyQualifiedClassName)
    mirror.reflectModule(module).instance.asInstanceOf[T]
  }

}
