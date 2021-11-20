package it.ldsoftware.starling.extensions

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object JacksonExtension {

  val objectMapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  private val GenericTypeRef = new TypeReference[Map[String, Any]] {}
  private val GenericSeqTypeRef = new TypeReference[Seq[Map[String, Any]]] {}

  implicit class JsonOps(json: String) {

    def asMap: Map[String, Any] = objectMapper.readValue(json, GenericTypeRef)

    def jsonGet(propName: String): SubProperty =
      asMap.get(propName) match {
        case Some(prop) if prop.isInstanceOf[Seq[_]] => SubArray(objectMapper.convertValue(prop, GenericSeqTypeRef))
        case Some(prop)                              => SubGeneric(objectMapper.convertValue(prop, GenericTypeRef))
        case None                                    => SubGeneric(Map())
      }

    def jsonGet(optProp: Option[String]): SubProperty =
      optProp match {
        case Some(subProp) => jsonGet(subProp)
        case None          => SubGeneric(asMap)
      }

  }

  sealed trait SubProperty
  case class SubArray(seq: Seq[Map[String, Any]]) extends SubProperty
  case class SubGeneric(gen: Map[String, Any]) extends SubProperty

}
