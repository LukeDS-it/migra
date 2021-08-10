package it.ldsoftware.starling

import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.engine.ConsumerBuilder

object TestHocon extends App {

  val conf = ConfigFactory.parseResources("consumer-test.conf")

  conf.getConfigList("consume") forEach { c =>
    val cType = c.getString("type")
    val cName = s"it.ldsoftware.starling.engine.consumers.$cType"
    val mirror = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)
    val module = mirror.staticModule(cName)
    val companion = mirror.reflectModule(module).instance.asInstanceOf[ConsumerBuilder]
    val consumer = companion(c.getConfig("config"))

    println(s"Configuring $cType with ${c.getConfig("config")}")
  }

}
