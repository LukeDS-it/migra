package it.ldsoftware.starling.extensions

import java.sql.Connection
import scala.io.Source

object UsableExtensions {

  implicit class UsableSource(source: Source) {
    def use[T](action: Source => T): T = {
      val result = action(source)
      source.close()
      result
    }
  }

  implicit class UsableConnection(connection: Connection) {
    def use[T](action: Connection => T): T = {
      val result = action(connection)
      connection.close()
      result
    }
  }

  implicit class LetOperations[T](any: T) {
    def let[P](action: T => P): P = action(any)
  }

  implicit class MutateOperations[T](any: T) {
    def mutate(action: T => Unit): T = {
      action(any)
      any
    }
  }

}
