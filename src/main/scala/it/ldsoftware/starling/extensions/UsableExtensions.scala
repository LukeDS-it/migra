package it.ldsoftware.starling.extensions

object UsableExtensions {

  implicit class UsableCloseable[C <: AutoCloseable](closeable: C) {
    def use[T](action: C => T): T = {
      val result = action(closeable)
      closeable.close()
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
