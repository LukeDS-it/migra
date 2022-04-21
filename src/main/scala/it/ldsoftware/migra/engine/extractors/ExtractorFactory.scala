package it.ldsoftware.migra.engine.extractors

import com.typesafe.config.Config
import it.ldsoftware.migra.engine.{getBuilder, Extractor, ExtractorBuilder, ProcessContext}
import it.ldsoftware.migra.extensions.ConfigExtensions._

object ExtractorFactory {

  final val ExtractorPath = "extract"
  final val ExtractorTypePath = "type"
  final val ExtractorConfigPath = "config"

  def getExtractors(config: Config, pc: ProcessContext): List[Extractor] =
    config.getConfigSList(ExtractorPath).map { c =>
      val cType = c.getString(ExtractorTypePath)
      val cName = s"it.ldsoftware.migra.engine.extractors.$cType"
      val builder = getBuilder[ExtractorBuilder](cName)
      builder(c.getConfig(ExtractorConfigPath), pc)
    }

}
