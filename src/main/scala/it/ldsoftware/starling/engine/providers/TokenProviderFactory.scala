package it.ldsoftware.starling.engine.providers

import com.typesafe.config.Config
import it.ldsoftware.starling.engine.{ProcessContext, TokenProvider, TokenProviderBuilder, getBuilder}
import it.ldsoftware.starling.extensions.ConfigExtensions.ConfigOperations

object TokenProviderFactory {

  final val TokenProviderPath = "tokenProviders"
  final val TokenProviderTypePath = "type"
  final val TokenProviderConfigPath = "config"
  final val TokenProviderNamePath = "alias"

  def getTokenProviders(config: Config, pc: ProcessContext): List[TokenProvider] =
    config.getConfigSList(TokenProviderPath).map { c =>
      val cType = c.getString(TokenProviderTypePath)
      val cName = s"it.ldsoftware.starling.engine.providers.$cType"
      val builder = getBuilder[TokenProviderBuilder](cName)
      builder(c.getString(TokenProviderNamePath), c.getConfig(TokenProviderConfigPath), pc)
    }

}
