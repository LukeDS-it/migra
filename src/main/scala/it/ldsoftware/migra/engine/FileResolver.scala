package it.ldsoftware.migra.engine

trait FileResolver {

  def retrieveFile(fileName: String): String

}
