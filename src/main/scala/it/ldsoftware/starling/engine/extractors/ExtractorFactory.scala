package it.ldsoftware.starling.engine.extractors

import com.typesafe.config.Config
import it.ldsoftware.starling.engine.util.ReflectionFactory
import it.ldsoftware.starling.engine.{Extractor, ExtractorBuilder}
import it.ldsoftware.starling.extensions.ConfigExtensions._

object ExtractorFactory {

  final val ExtractorPath = "extract"
  final val ExtractorTypePath = "type"
  final val ExtractorConfigPath = "config"

  def getExtractors(config: Config): List[Extractor] =
    config.getConfigSList(ExtractorPath).map { c =>
      val cType = c.getString(ExtractorTypePath)
      val cName = s"it.ldsoftware.starling.engine.extractors.$cType"
      val builder = ReflectionFactory.getBuilder[ExtractorBuilder](cName)
      builder(c.getConfig(ExtractorConfigPath))
    }

}
