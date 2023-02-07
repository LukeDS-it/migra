package it.ldsoftware.migra.engine.extractors

import com.typesafe.config.Config
import it.ldsoftware.migra.engine.extractors.ScalaEvalExtractor.WrapperFunction
import it.ldsoftware.migra.engine.{Extracted, ExtractionResult, Extractor, ExtractorBuilder, ProcessContext}
import it.ldsoftware.migra.extensions.ScriptEngineExtensions.{ScriptEnginePool, ScriptEngineWrapper}

import javax.script.ScriptEngine
import scala.concurrent.{ExecutionContext, Future}

trait ScriptExtractor extends Extractor {

  val script: String
  val enginePool: ScriptEnginePool
  val callerFunction: String

  override def doExtract(): Future[Seq[ExtractionResult]] =
    enginePool.getFreeEngine.map { engine =>
      engine.execute { e =>
        e.put("initialValue", initialValue)
        e.eval(script)
        e.eval(callerFunction).asInstanceOf[Seq[Extracted]]
      }
        .map(Right(_))
    }

}

trait ScriptExtractorBuilder extends ExtractorBuilder {

  val WrapperFunction: String

  val Language: String

  def getNewInstance(script: String, pool: ScriptEnginePool, config: Config): ExecutionContext => ScriptExtractor

  override def apply(config: Config, pc: ProcessContext): Extractor = {
    val script = config.getString("type") match {
      case "inline" => String.format(WrapperFunction, config.getString("script"))
      case "file"   => pc.retrieveFile(config.getString("file"))
      case x        => throw new Error(s"Cannot handle script of type $x")
    }
    implicit val ec: ExecutionContext = pc.executionContext

    val enginePool = new ScriptEnginePool(Language, pc.appConfig)

    getNewInstance(script, enginePool, config)(ec)
  }

}
