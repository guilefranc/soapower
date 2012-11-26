package controllers

import play.Logger
import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import java.nio.charset.Charset

import models._

object Soap extends Controller {

  def index(environment: String, localTarget: String) = Action { implicit request =>

    Logger.info("Request on environment:" + environment + " localTarget:" + localTarget)
    Logger.debug("request:" + request.body.asText)

    Service.findByLocalTargetAndEnvironmentName(localTarget, environment).map { service =>
      val client = new Client(service, request.body.asXml.get.toString, request.headers.toSimpleMap)
      client.sendRequest
      client.waitForResponse

      SimpleResult(
        header = ResponseHeader(client.response.status, client.response.headers),
        body = Enumerator(client.response.body)).withHeaders(("ProxyVia" -> "soapower"))

    }.getOrElse {
      val err = "environment " + environment + " with localTarget " + localTarget + " unknown"
      Logger.error(err)
      BadRequest(err)
    }

  }

  def printrequest(implicit r: play.api.mvc.RequestHeader) = {
    Logger.info("method:" + r)
    Logger.info("headers:" + r.headers)
    //Logger.info("SoapAction:" + r.headers("SOAPACTION"))
    Logger.info("path:" + r.path)
    Logger.info("uri:" + r.uri)
    Logger.info("host:" + r.host)
    Logger.info("rawQueryString:" + r.rawQueryString)
  }

}
