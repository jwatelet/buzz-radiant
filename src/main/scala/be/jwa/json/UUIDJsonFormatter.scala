package be.jwa.json

import java.util.UUID

import spray.json.{DeserializationException, JsString, JsValue, JsonFormat}

trait UUIDJsonFormatter {

  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)

    def read(value: JsValue): UUID = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ => throw DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }

}
