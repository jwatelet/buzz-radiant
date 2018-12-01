package be.jwa.directive

import akka.http.scaladsl.model.HttpMethods.OPTIONS
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpHeader, HttpResponse}
import akka.http.scaladsl.server.Directives.{complete, handleRejections, mapInnerRoute, respondWithHeaders}
import akka.http.scaladsl.server.{Directive0, MethodRejection, RejectionHandler}

trait CorsSupport {

  val corsAllowOrigins: List[String] = List("*")

  val corsAllowedHeaders: List[String] = List("Origin", "X-Requested-With", "Content-Type", "Accept", "Accept-Encoding", "Accept-Language", "Host", "Referer", "User-Agent", "Authorization")

  val corsAllowCredentials: Boolean = true

  val corsMaxAge: Int = 20 * 60 * 60 * 24

  protected def optionsCorsHeaders: List[HttpHeader] = List[HttpHeader](
    `Access-Control-Allow-Headers`(corsAllowedHeaders.mkString(", ")),
    `Access-Control-Max-Age`(corsMaxAge)
  )

  protected def corsRejectionHandler(allowOrigin: `Access-Control-Allow-Origin`): RejectionHandler = RejectionHandler
    .newBuilder()
    .handleAll[MethodRejection] { rejections =>
    var methods = rejections map (_.supported)
    methods = methods :+ OPTIONS

    complete(HttpResponse().withHeaders(
      `Access-Control-Allow-Methods`(methods) ::
        allowOrigin ::
        optionsCorsHeaders
    ))
  }
    .result()

  private def originToAllowOrigin(origin: Origin): Option[`Access-Control-Allow-Origin`] =
    if (corsAllowOrigins.contains("*") || corsAllowOrigins.contains(origin.value))
      origin.origins.headOption.map(`Access-Control-Allow-Origin`.apply)
    else
      None

  def cors[T]: Directive0 = mapInnerRoute { route =>
    context =>
      ((context.request.method, context.request.header[Origin].flatMap(originToAllowOrigin)) match {
        case (OPTIONS, Some(allowOrigin)) =>
          handleRejections(corsRejectionHandler(allowOrigin)) {
            respondWithHeaders(allowOrigin, `Access-Control-Allow-Credentials`(corsAllowCredentials)) {
              route
            }
          }
        case (_, Some(allowOrigin)) =>
          respondWithHeaders(allowOrigin, `Access-Control-Allow-Credentials`(corsAllowCredentials)) {
            route
          }
        case (_, _) =>
          route
      }) (context)
  }
}
