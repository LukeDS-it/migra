package it.ldsoftware.starling.workers

object ReflectionFactory {

  def getBuilder[T](fullyQualifiedClassName: String): T = {
    val mirror = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)
    val module = mirror.staticModule(fullyQualifiedClassName)
    mirror.reflectModule(module).instance.asInstanceOf[T]
  }

}
