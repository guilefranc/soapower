package controllers

import play.api.mvc._
import play.api.libs.json._
import models._
import models.UtilDate._
import play.api.libs.iteratee.Enumerator
import play.api.http.{ContentTypes, HeaderNames}
import scala.xml.PrettyPrinter
import org.xml.sax.SAXParseException
import play.api.Logger
import java.net.URLDecoder
import com.fasterxml.jackson.databind.JsonMappingException

case class Search(environmentId: Long)

object Search extends Controller {

  private val UTF8 = "UTF-8"

  def listDatatable(group: String, environment: String, serviceAction: String, minDate: String, maxDate: String, status: String, sSearch: String, iDisplayStart: Int, iDisplayLength: Int) = Action {
    val page: Page[(RequestData)] = RequestData.list(group, environment, URLDecoder.decode(serviceAction, UTF8), getDate(minDate).getTime, getDate(maxDate, v23h59min59s, true).getTime, status, (iDisplayStart - 1), iDisplayLength, sSearch)
    Logger.debug("iTotalRecords {}" + Json.toJson(iDisplayLength))
    Logger.debug("iTotalDisplayRecords {}" + Json.toJson(page.total))
    Logger.debug("data {}" + Json.toJson(page.items))

    Ok(Json.toJson(Map(
      "iTotalRecords" -> Json.toJson(iDisplayLength),
      "iTotalDisplayRecords" -> Json.toJson(page.total),
      "data" -> Json.toJson(page.items)))).as(JSON)
  }


  /**
   * Used to download or render the request
   * @param id of the requestData to download
   * @param asFile
   * @return
   */
  def downloadRequest(id: Long, asFile: Boolean) = Action {
    // Retrieve the request content and the requestContentType in a Tuple
    val request = RequestData.loadRequest(id)

    val format = request._2 match {
      case "application/xml" | "text/xml" =>
        "XML"
      case "application/json" =>
        "JSON"
      case _ =>
        "TEXT"
    }

    request._1 match {
      case Some(str: String) => {
        downloadInCorrectFormat(request._1.get, id, format, asFile, true)
      }
      case _ =>
        NotFound("The request does not exist")
    }
  }

  /**
   * Used to download or render the response
   * @param id of the requestData to download
   * @param asFile
   * @return
   */
  def downloadResponse(id: Long, asFile: Boolean) = Action {
    val response = RequestData.loadResponse(id)

    val format = response._2 match {
      case "application/xml" | "text/xml" =>
        "XML"
      case "application/json" =>
        "JSON"
      case _ =>
        if (response._2.startsWith("application/xml") || (response._2.startsWith("text/xml")))
        {
          "XML"
        } else if (response._2.startsWith("application/json")) {
          "JSON"
        } else {
          "TEXT"
        }
    }

    response._1 match {
      case Some(str: String) => {
        downloadInCorrectFormat(response._1.get, id, format, asFile, false)
      }
      case _ =>
        NotFound("The response does not exist")
    }
  }


  /**
   * Download the response / request in the correct format
   * @param str the requestContent
   * @param id of the requestData
   * @param format format in which the requestContent will be displayed
   * @param asFile
   * @param isRequest
   * @return
   */
  def downloadInCorrectFormat(str: String, id: Long, format: String, asFile: Boolean, isRequest: Boolean) = {
    var filename = ""
    if (isRequest) {
      filename = "request-" + id
    }
    else {
      filename = "response-" + id
    }

    var contentInCorrectFormat = ""

    format match {
      case "XML" => {
        try {
          contentInCorrectFormat = new PrettyPrinter(250, 4).format(scala.xml.XML.loadString(str))
        } catch {
          case e: SAXParseException => contentInCorrectFormat = str
        }
        filename += ".xml"

        var result = SimpleResult(
          header = ResponseHeader(play.api.http.Status.OK),
          body = Enumerator(contentInCorrectFormat.getBytes))
        if (asFile) {
          result = result.withHeaders((HeaderNames.CONTENT_DISPOSITION, "attachment; filename=" + filename))
          result.as(XML)
        } else {
          result.as(TEXT)
        }
      }
      case "JSON" => {
        try {
          contentInCorrectFormat = Json.parse(str).toString
          filename += ".json"
        }
        catch {
          case e: JsonMappingException =>
          {
            contentInCorrectFormat = str
          }
        }
        var result = SimpleResult(
          header = ResponseHeader(play.api.http.Status.OK),
          body = Enumerator(contentInCorrectFormat.getBytes))
        if (asFile) {
          result = result.withHeaders((HeaderNames.CONTENT_DISPOSITION, "attachment; filename=" + filename))
          result.as(JSON)
        } else {
          result.as(TEXT)
        }
      }
      case _ => {
        filename += ".txt"
        var result = SimpleResult(
          header = ResponseHeader(play.api.http.Status.OK),
          body = Enumerator(str.getBytes))
        if (asFile) {
          result = result.withHeaders((HeaderNames.CONTENT_DISPOSITION, "attachment; filename=" + filename))
        }
        result.as(TEXT)
      }
    }
  }
}

