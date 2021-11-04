package it.ldsoftware.starling.engine.extractors

import akka.stream.Materializer
import com.typesafe.config.Config
import it.ldsoftware.starling.engine.util.ReflectionFactory
import it.ldsoftware.starling.engine.{Extractor, ExtractorBuilder}
import it.ldsoftware.starling.extensions.ConfigExtensions._

import scala.concurrent.ExecutionContext

object ExtractorFactory {

  final val ExtractorPath = "extract"
  final val ExtractorTypePath = "type"
  final val ExtractorConfigPath = "config"

  def getExtractors(config: Config, ec: ExecutionContext, mat: Materializer): List[Extractor] =
    config.getConfigSList(ExtractorPath).map { c =>
      val cType = c.getString(ExtractorTypePath)
      val cName = s"it.ldsoftware.starling.engine.extractors.$cType"
      val builder = ReflectionFactory.getBuilder[ExtractorBuilder](cName)
      builder(c.getConfig(ExtractorConfigPath), ec, mat)
    }

}
