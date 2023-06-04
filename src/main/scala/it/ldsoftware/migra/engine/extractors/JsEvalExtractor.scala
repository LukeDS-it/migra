package it.ldsoftware.migra.engine.extractors
import com.typesafe.config.Config
import it.ldsoftware.migra.engine.{Extracted, Extractor}
import it.ldsoftware.migra.extensions.ScriptEngineExtensions.ScriptEnginePool

import java.util
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava, MapHasAsScala}
import scala.concurrent.ExecutionContext

class JsEvalExtractor(
    override val script: String,
    override val enginePool: ScriptEnginePool,
    override val config: Config,
    override val initialValue: Extracted = Map()
)(implicit val ec: ExecutionContext)
    extends ScriptExtractor {

  override val callerFunction: String = "produce(initialValue)"

  override def toPipedExtractor(data: Extracted): Extractor =
    new JsEvalExtractor(script, enginePool, config, data)

  override def summary: String = s"JsEvalExtractor failed to execute script with data $initialValue"

  override def fixInput(initialValue: Extracted): Any = {
    val map = new util.HashMap[String, Any]()
    initialValue.foreach { case (str, value) =>
      map.put(str, value)
    }
    map
  }

  override def toSeqExtracted(engineResult: Object): Seq[Extracted] =
    engineResult
      .asInstanceOf[java.util.List[java.util.Map[String, Any]]]
      .asScala
      .toList
      .map { m =>
        m.asScala.toMap
      }

}

object JsEvalExtractor extends ScriptExtractorBuilder {
  override val WrapperFunction: String =
    """
      |function produce(data) {
      |  return %s;
      |}
      |""".stripMargin

  override val Language: String = "JavaScript"

  override def getNewInstance(
      script: String,
      pool: ScriptEnginePool,
      config: Config
  ): ExecutionContext => ScriptExtractor =
    ec => new JsEvalExtractor(script, pool, config)(ec)
}
