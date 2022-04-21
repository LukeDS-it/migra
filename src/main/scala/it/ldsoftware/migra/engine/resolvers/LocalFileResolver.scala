package it.ldsoftware.migra.engine.resolvers

import it.ldsoftware.migra.engine.FileResolver
import it.ldsoftware.migra.extensions.IOExtensions.FileFromStringExtensions

import java.io.File
import scala.annotation.tailrec

class LocalFileResolver(processFile: File) extends FileResolver {

  private val relativeRoot = processFile.getAbsolutePath.substring(0, processFile.getAbsolutePath.lastIndexOf("/"))

  @tailrec final override def retrieveFile(fileName: String): String =
    if (fileName.startsWith("/")) fileName.readFile
    else retrieveFile(s"$relativeRoot/$fileName")

}
