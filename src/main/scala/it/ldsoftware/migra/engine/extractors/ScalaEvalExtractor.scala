package it.ldsoftware.migra.engine.extractors

import com.typesafe.config.Config
import it.ldsoftware.migra.engine._
import it.ldsoftware.migra.extensions.ScriptEngineExtensions.ScriptEnginePool

import scala.concurrent.ExecutionContext

class ScalaEvalExtractor(
    override val script: String,
    override val enginePool: ScriptEnginePool,
    override val config: Config,
    override val initialValue: Extracted = Map()
)(implicit
    val ec: ExecutionContext
) extends ScriptExtractor {

  override val callerFunction = "produce(initialValue.asInstanceOf[Map[String, Any]])"

  override def toPipedExtractor(data: Extracted): Extractor =
    new ScalaEvalExtractor(script, enginePool, config, data)

  override def summary: String = s"ScalaEvalExtractor failed to execute script with data $initialValue"
}

object ScalaEvalExtractor extends ScriptExtractorBuilder {

  override val WrapperFunction: String =
    """
      |def produce(data: Map[String, Any]): Seq[Map[String, Any]] = {
      | %s
      |}
      |""".stripMargin

  override val Language: String = "scala"

  override def getNewInstance(
      script: String,
      pool: ScriptEnginePool,
      config: Config
  ): ExecutionContext => ScriptExtractor =
    ec => new ScalaEvalExtractor(script, pool, config)(ec)

}
