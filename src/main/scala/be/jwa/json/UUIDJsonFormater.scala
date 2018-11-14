package be.jwa.json

import java.util.UUID

import spray.json.{DeserializationException, JsString, JsValue, JsonFormat}

trait UUIDJsonFormater {

  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)

    def read(value: JsValue): UUID = value match {
      case JsString(x) => UUID.fromString(x)
      case _ => throw DeserializationException("String expected")
    }
  }

}
