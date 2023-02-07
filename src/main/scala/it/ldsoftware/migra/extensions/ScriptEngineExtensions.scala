package it.ldsoftware.migra.extensions

import it.ldsoftware.migra.configuration.AppConfig

import javax.script.{ScriptContext, ScriptEngine, ScriptEngineManager}
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

object ScriptEngineExtensions {

  class ScriptEnginePool(language: String, appConfig: AppConfig)(implicit ec: ExecutionContext) {

    private val pool = (0 until appConfig.maxScriptEngines).map { _ =>
      new ScriptEngineWrapper(new ScriptEngineManager().getEngineByName(language))
    }

    def getFreeEngine: Future[ScriptEngineWrapper] =
      Future {
        pool.synchronized {
          @tailrec def findFirstFree: ScriptEngineWrapper = {
            val opt = pool.find(_.free)
            if (opt.isEmpty) findFirstFree else opt.get
          }

          val engineWrapper = findFirstFree
          engineWrapper.lock()
          engineWrapper
        }
      }

  }

  class ScriptEngineWrapper(engine: ScriptEngine) {

    var free: Boolean = true

    def lock(): Unit =
      free = false

    def execute[T](function: ScriptEngine => T): T =
      try
        function(engine)
      finally {
        engine.setBindings(engine.createBindings(), ScriptContext.GLOBAL_SCOPE)
        free = true
      }

  }

}
