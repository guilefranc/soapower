package controllers.admin

import play.api.mvc._
import models._
import play.api.libs.json._

object Mocks extends Controller {

  implicit val mockFormat = Json.format[Mock]

  /**
   * Return all Mocks in Json Format
   * @return JSON
   */
  def findAll = Action {
    implicit request =>
      val data = Mock.list
      Ok(Json.toJson(data)).as(JSON)
  }

  /**
   * List to Datable table.
   *
   * @return JSON
   */
  def listDatatable(mockGroup: String) = Action {
    implicit request =>

      var data: List[Mock] = null.asInstanceOf[List[Mock]]

      if (mockGroup != "all") {
        data = Mock.list(mockGroup)
      } else {
        data = Mock.list
      }

      Ok(Json.toJson(Map(
        "iTotalRecords" -> Json.toJson(data.size),
        "iTotalDisplayRecords" -> Json.toJson(data.size),
        "data" -> {
          Json.toJson(data)
        }
      ))).as(JSON)
  }

  /**
   * Display the 'edit form' of a existing Mock.
   *
   * @param id Id of the mock to edit
   */
  def edit(id: Long) = Action {
    Mock.findById(id).map {
      mock =>
        Ok(Json.toJson(mock)).as(JSON)
    }.getOrElse(NotFound)
  }


  /**
   * Insert or update a mock.
   */
  def save(id: Long) = Action(parse.json) {
    request =>
      request.body.validate(mockFormat).map {
        mock =>
          try {
            if (id < 0) Mock.insert(mock)
            else Mock.update(mock)
            Ok(Json.toJson("Succesfully save mock."))
          } catch {
            case e: Throwable => {
              BadRequest("Detected error:" + e.getMessage)
            }
          }

      }.recoverTotal {
        e => BadRequest("Detected error:" + JsError.toFlatJson(e))
      }
  }

  /**
   * Handle mock deletion.
   */
  def delete(id: Long) = Action(parse.tolerantText) {
    request =>
      Mock.delete(id)
      Ok("deleted")
  }
}
