package it.ldsoftware.starling.extensions

import it.ldsoftware.starling.extensions.UsableExtensions.UsableCloseable

import java.io.File
import scala.io.Source

object IOExtensions {

  implicit class FileFromStringExtensions(fileName: String) {
    def readFile: String = Source.fromFile(fileName).use { s =>
      s.getLines().mkString("\n")
    }
  }

  implicit class FileFromFileExtensions(file: File) {
    def readFile: String = Source.fromFile(file).use { s =>
      s.getLines().mkString("\n")
    }
  }

}
