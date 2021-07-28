package it.ldsoftware.starling.workers.extractors

import com.typesafe.config.Config
import it.ldsoftware.starling.workers.ReflectionFactory
import it.ldsoftware.starling.workers.model._

object ExtractorFactory {

  final val ExtractorPath = "extract"
  final val ExtractorTypePath = "type"
  final val ExtractorConfigPath = "config"

  def getExtractors(config: Config): List[Extractor] =
    config.getConfigSList(ExtractorPath).map { c =>
      val cType = c.getString(ExtractorTypePath)
      val cName = s"it.ldsoftware.starling.workers.extractors.$cType"
      val builder = ReflectionFactory.getBuilder[ExtractorBuilder](cName)
      builder(c.getConfig(ExtractorConfigPath))
    }

}
