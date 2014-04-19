package models

import java.util.{GregorianCalendar, Calendar, Date}
import play.api.Play.current
import play.api.libs.json._
import java.util.zip.{GZIPOutputStream, GZIPInputStream}
import java.nio.charset.Charset

import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.duration._
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.{Await, Future}
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.core.commands.RawCommand

case class RequestData(
                        id: Option[BSONObjectID],
                        sender: String,
                        var serviceAction: String,
                        environmentName: String,
                        serviceId: String,
                        var request: String,
                        var requestHeaders: Map[String, String],
                        var contentType: String,
                        var requestCall: String,
                        startTime: Date,
                        var response: String,
                        var responseHeaders: Map[String, String],
                        var timeInMillis: Long,
                        var status: Int,
                        var purged: Boolean,
                        var isMock: Boolean) {

  var responseBytes: Array[Byte] = null


  def this(sender: String, serviceAction: String, environnmentName: String, serviceId: String, contentType: String) =
    this(Some(BSONObjectID.generate), sender, serviceAction, environnmentName, serviceId, null, null, contentType, null, new Date, null, null, -1, -1, false, false)

  /**
   * Add serviceAction in cache if neccessary.
   */
  def storeServiceActionAndStatusInCache() {
    //TODO
    /*
    if (!RequestData.serviceActionOptions.exists(p => p._1 == serviceAction)) {
      Logger.info("ServiceAction " + serviceAction + " not found in cache : add to cache")
      val inCache = RequestData.serviceActionOptions ++ (List((serviceAction, serviceAction)))
      Cache.set(RequestData.keyCacheServiceAction, inCache)
    }
    if (!RequestData.statusOptions.exists(p => p._1 == status)) {
      Logger.info("Status " + serviceAction + " not found in cache : add to cache")
      val inCache = RequestData.statusOptions ++ (List((status, status)))
      Cache.set(RequestData.keyCacheStatusOptions, inCache)
    }
    */
  }
}

object RequestData {

  val keyCacheServiceAction = "serviceaction-options"
  val keyCacheStatusOptions = "status-options"
  val keyCacheMinStartTime = "minStartTime"

  /*implicit def rowToByteArray: Column[Array[Byte]] = Column.nonNull {
    (value, meta) =>
      val MetaDataItem(qualified, nullable, clazz) = meta
      value match {
        case data: Array[Byte] => Right(data)
        case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " to Byte Array for column " + qualified))
      }
  }*/

  implicit val requestDataFormat = Json.format[RequestData]

  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("requestData")

  /**
   * Anorm Byte conversion
   */
  //def bytes(columnName: String): RowParser[Array[Byte]] = get[Array[Byte]](columnName)(implicitly[Column[Array[Byte]]])

  /**
   * Title of csvFile. The value is the order of title.
   */
  val csvTitle = Map("key" -> 0, "id" -> 1, "serviceAction" -> 2, "startTime" -> 3, "timeInMillis" -> 4, "environmentName" -> 5)

  val csvKey = "requestDataStat"

  //def fetchCsv(): List[String] = DB.withConnection {
  def fetchCsv(): List[String] = {
    //TODO
    ???
  }

  /**
   * Parse required parts of a RequestData from a ResultSet in order to replay the request
   */
  /* TODO
  val forReplay = {
    get[Pk[Long]]("request_data.id") ~
      str("request_data.sender") ~
      str("request_data.serviceAction") ~
      long("request_data.environmentId") ~
      long("request_data.serviceId") ~
      bytes("request_data.request") ~
      str("request_data.requestHeaders") ~
      str("request_data.contentType") map {
      case id ~ sender ~ serviceAction ~ environnmentId ~ serviceId ~ request ~ requestHeaders ~ contentType =>
        val headers = UtilConvert.headersFromString(requestHeaders)
        new RequestData(id, sender, serviceAction, environnmentId, serviceId, uncompressString(request), headers, contentType, null, null, null, null, -1, -1, false, false)
    }
  }

  val forRESTReplay = {
    get[Pk[Long]]("request_data.id") ~
      str("request_data.sender") ~
      str("request_data.serviceAction") ~
      long("request_data.environmentId") ~
      long("request_data.serviceId") ~
      bytes("request_data.request") ~
      str("request_data.requestHeaders") ~
      str("request_data.requestCall") map {
      case id ~ sender ~ serviceAction ~ environnmentId ~ serviceId ~ request ~ requestHeaders ~ requestCall =>
        val headers = UtilConvert.headersFromString(requestHeaders)
        new RequestData(id, sender, serviceAction, environnmentId, serviceId, uncompressString(request), headers, null, requestCall, null, null, null, -1, -1, false, false)
    }
  }
  */

  /**
   * Construct the Map[String, String] needed to fill a select options set.
   */
  //def serviceActionOptions: Seq[(String, String)] = DB.withConnection {
  def serviceActionOptions: Seq[(String, String)] = {
    //TODO
    ???
    /*
      implicit connection =>
        Cache.getOrElse[Seq[(String, String)]](keyCacheServiceAction) {
          Logger.debug("RequestData.ServiceAction not found in cache: loading from db")
          SQL("select distinct(serviceAction) from request_data order by serviceAction asc").as((get[String]("serviceAction") ~ get[String]("serviceAction")) *).map(flatten)
        }
    */
  }

  /**
   * Construct the Map[String, String] needed to fill a select options set.
   */
  //def statusOptions: Seq[(Int, Int)] = DB.withConnection {
  def statusOptions: Future[BSONDocument] = {
    collection
    val command = RawCommand(BSONDocument("distinct" -> "requestData", "key" -> "status", "query" -> BSONDocument()))
    ReactiveMongoPlugin.db.command(command) // result is Future[BSONDocument]
  }

  /**
   * find Min Date.
   */
  //def getMinStartTime: Option[Date] = DB.withConnection {
  def getMinStartTime: Option[Date] = {
    //TODO
    ???
    /*
    implicit connection =>
      Cache.getOrElse[Option[Date]](keyCacheMinStartTime) {
        Logger.debug("MinStartTime not found in cache: loading from db")
        SQL("select min(startTime) as startTimeMin from request_data").as(scalar[Option[Date]].single)
      }
    */
  }

  /**
   * Insert a new RequestData.
   *
   * @param requestData the requestData
   */
  def insert(requestData: RequestData): Option[BSONObjectID] = {

    //TODO
    ???
    /*
    var contentRequest: String = null
    var contentResponse: String = null

    val f = Environment.findById(requestData.environmentId).map(e => e)
    val environment = Await result(f, 1.seconds)

    val service = Service.findById(requestData.serviceId).get
    val date = new Date()
    val gcal = new GregorianCalendar()
    gcal.setTime(date)
    gcal.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format

    if (!service.recordData || !environment.get.recordData) {
      Logger.debug("Data not recording for this service or this environment")
      return None
    } else if (!service.recordContentData) {
      val msg = "Content Data not recording for this service. See Admin."
      requestContent = compressString(msg)
      responseContent = compressString(msg)
      Logger.debug(msg)
    } else if (!environment.recordContentData) {
      val msg = "Content Data not recording for this environment. See Admin."
      requestContent = compressString(msg)
      responseContent = compressString(msg)
      Logger.debug(msg)
    } else if (requestData.status != 200 || (
      environment.hourRecordContentDataMin <= gcal.get(Calendar.HOUR_OF_DAY) &&
      environment.hourRecordContentDataMax > gcal.get(Calendar.HOUR_OF_DAY))) {
      // Record Data if it is a soap fault (status != 200) or
      // if we can record data with environment's configuration (hours of recording)
      requestContent = compressString(requestData.request)

      def transferEncodingResponse = requestData.responseHeaders.filter {
        _._1 == HeaderNames.CONTENT_ENCODING
      }

      transferEncodingResponse.get(HeaderNames.CONTENT_ENCODING) match {
        case Some("gzip") =>
          Logger.debug("Response in gzip Format")
          contentResponse = "TODO response in gzip format" // TODO requestData.responseBytes
        case _ =>
          Logger.debug("Response in plain Format")
          contentResponse = requestData.response
      }
    } else {
      val msg = "Content Data not recording. Record between " + environment.get.hourRecordContentDataMin + "h to " + environment.get.hourRecordContentDataMax + "h for this environment."
      contentRequest = msg
      contentResponse = msg
      Logger.debug(msg)
    }

    requestData.request = contentRequest
    requestData.response = contentResponse
*/

    collection.insert(requestData).map {
      lastError =>
        Logger.debug(s"Successfully inserted RequestData with LastError: $lastError")
    }
    requestData.id
  }

  /**
   * Insert a new RequestData.
   */
  def insertStats(environmentId: Long, serviceAction: String, startTime: Date, timeInMillis: Long) = {
    /*
    try {

      DB.withConnection {
        implicit connection =>

          val purgedStats = SQL(
            """
            delete from request_data
            where isStats = 'true'
            and environmentId = {environmentId}
            and startTime >= {startTime} and startTime <= {startTime}
            and serviceAction = {serviceAction}
            """
          ).on(
            'serviceAction -> serviceAction,
            'environmentId -> environmentId,
            'startTime -> startTime).executeUpdate()

          Logger.debug("Purged " + purgedStats + " existing stats for:" + environmentId + " serviceAction: " + serviceAction + " and startTime:" + startTime)

          SQL(
            """
            insert into request_data
              (sender, serviceAction, environmentId, request, requestHeaders, startTime, response, responseHeaders, timeInMillis, status, isStats, isMock) values (
              '', {serviceAction}, {environmentId}, '', '', {startTime}, '', '', {timeInMillis}, 200, 'true', 'false'
            )
            """).on(
            'serviceAction -> serviceAction,
            'environmentId -> environmentId,
            'startTime -> startTime,
            'timeInMillis -> timeInMillis).executeUpdate()
      }
    } catch {
      case e: Exception => Logger.error("Error during insertion of RequestData Stats", e)
    }
    */
  }

  /**
   * Delete data (request & reponse) between min and max date
   * @param environmentIn environmement or "" / all if all
   * @param minDate min date
   * @param maxDate max date
   * @param user use who delete the data : admin or akka
   */
  def deleteRequestResponse(environmentIn: String, minDate: Date, maxDate: Date, user: String): Int = {
    ???
    /*
    Logger.debug("Environment:" + environmentIn + " mindate:" + minDate.getTime + " maxDate:" + maxDate.getTime)
    Logger.debug("EnvironmentSQL:" + sqlAndEnvironnement(environmentIn))

    //val d = new Date()
    //val deleted = "deleted by " + user + " " + d.toString

    DB.withConnection {
      implicit connection =>
        val purgedRequests = SQL(
          """
            update request_data
            set response = '',
            request = '',
            requestHeaders = '',
            responseHeaders = '',
            purged = 'true'
            where startTime >= {minDate} and startTime <= {maxDate} and purged = 'false' and isStats = 'false'
          """
            + sqlAndEnvironnement(environmentIn)).on(
          'minDate -> minDate,
          'maxDate -> maxDate).executeUpdate()

        Cache.remove(keyCacheServiceAction)
        Cache.remove(keyCacheStatusOptions)
        purgedRequests
    }
    */
  }

  /**
   * Delete entries between min and max date
   * @param environmentIn environmement or "" / all if all
   * @param minDate min date
   * @param maxDate max date
   */
  def delete(environmentIn: String, minDate: Date, maxDate: Date): Int = {
    //TODO
    ???
    /*
    DB.withConnection {
      implicit connection =>
        val deletedRequests = SQL(
          """
            delete from request_data
            where startTime >= {minDate} and startTime < {maxDate}
            and isStats = 'false'
          """
            + sqlAndEnvironnement(environmentIn)).on(
          'minDate -> minDate,
          'maxDate -> maxDate).executeUpdate()
        Cache.remove(keyCacheServiceAction)
        Cache.remove(keyCacheStatusOptions)
        deletedRequests
    }
    */
  }

  /**
   * Return a page of RequestData
   * @param groupName group name
   * @param environmentIn name of environnement, "all" default
   * @param serviceAction serviceAction, "all" default
   * @param minDate Min Date
   * @param maxDate Max Date
   * @param status Status
   * @param offset offset in search
   * @param pageSize size of line in one page
   * @return
   */
  def list(groupName: String, environmentIn: String, serviceAction: String, minDate: Date, maxDate: Date, status: String, offset: Int = 0, pageSize: Int = 10): Future[List[RequestData]] = {

    val query = BSONDocument()

    // TODO Group
    /*val group = Group.options.find(t => t._2 == groupName)

    if (group isDefined) query ++ ("groupdId" -> group.get._1)
    */

    // TODO Environment
    /*if (environmentIn != "all" && Environment.optionsAll.exists(t => t._2 == environmentIn))
      query ++ ("environmentId" -> Environment.optionsAll.find(t => t._2 == environmentIn).get._1)
      */

    if (serviceAction != "all") query ++ ("serviceAction" -> serviceAction)

    // Convert dates... bad perf anorm ?
    val g = new GregorianCalendar()
    g.setTime(minDate)
    val min = UtilDate.formatDate(g)
    g.setTime(maxDate)
    val max = UtilDate.formatDate(g)

    query ++ ("startTime" -> BSONDocument("&gte" -> min, "&lte" -> max))

    if (status != "all") query ++ ("status" -> status)

    // TODO offset
    // TODO pageSize

    collection.
      find(query).
      sort(Json.obj("startTime" -> -1)).
      cursor[RequestData].
      collect[List]()
  }

  /**
   * Load reponse times for given parameters
   */
  def findResponseTimes(groupName: String, environmentIn: String, serviceAction: String, minDate: Date, maxDate: Date, status: String, statsOnly: Boolean): List[(Long, String, Date, Long)] = {
    ???
    /*
      var whereClause = "where startTime >= {minDate} and startTime <= {maxDate}"
      whereClause += sqlAndStatus(status)
      Logger.debug("DDDDD==>" + whereClause)
      if (serviceAction != "all") whereClause += " and serviceAction = {serviceAction}"
      whereClause += sqlAndEnvironnement(environmentIn)

      val pair = sqlFromAndGroup(groupName, environmentIn)
      val fromClause = " from request_data " + pair._1
      whereClause += pair._2

      val params: Array[(Any, anorm.ParameterValue[_])] = Array(
        'minDate -> minDate,
        'maxDate -> maxDate,
        'serviceAction -> serviceAction)

      var sql = "select environmentId, serviceAction, startTime, timeInMillis " + fromClause + whereClause

      if (statsOnly) {
        sql += " and isStats = 'true' "
      }
      sql += " order by request_data.id asc"

      Logger.debug("SQL (findResponseTimes, g:" + groupName + ") ====> " + sql)

      DB.withConnection {
        implicit connection =>
        // explainPlan(sql, params: _*)
          SQL(sql)
            .on(params: _*)
            .as(get[Long]("environmentId") ~ get[String]("serviceAction") ~ get[Date]("startTime") ~ get[Long]("timeInMillis") *)
            .map(flatten)
      }*/

  }

  def loadAvgResponseTimesByAction(groupName: String, environmentName: String, status: String, minDate: Date, maxDate: Date, withStats: Boolean): List[(String, Long)] = {
    ???
    /*DB.withConnection {
  def loadAvgResponseTimesByAction(groupName: String, environmentName: String, status: String, minDate: Date, maxDate: Date, withStats: Boolean): List[(String, Long)] = {
    DB.withConnection {
      implicit connection =>

        var whereClause = " where startTime >= {minDate} and startTime <= {maxDate} "
        whereClause += sqlAndStatus(status)
        if (!withStats) whereClause += " and isStats = 'false' "

        Logger.debug("Load Stats with env:" + environmentName)
        whereClause += sqlAndEnvironnement(environmentName)

        val pair = sqlFromAndGroup(groupName, environmentName)
        val fromClause = " from request_data " + pair._1
        whereClause += pair._2

        val sql = "select serviceAction, timeInMillis " + fromClause + whereClause + " order by timeInMillis asc "

        Logger.debug("SQL (loadAvgResponseTimesByAction) ====> " + sql)

        val responseTimes = SQL(sql)
          .on(
          'minDate -> minDate,
          'maxDate -> maxDate)
          .as(get[String]("serviceAction") ~ get[Long]("timeInMillis") *)
          .map(flatten)

        val avgTimesByAction = responseTimes.groupBy(_._1).mapValues {
          e =>
            val times = e.map(_._2).toList
            val ninePercentiles = times.slice(0, times.size * 9 / 10)
            if (ninePercentiles.size > 0) {
              ninePercentiles.sum / ninePercentiles.size
            } else if (times.size == 1) {
              times.head
            } else {
              -1
            }
        }
        avgTimesByAction.toList.filterNot(_._2 == -1).sortBy(_._1)
    }*/
  }

  /**
   * Find all day before today, for environment given and state = 200.
   * @param environmentId environment id
   * @return list of unique date
   */
  def findDayNotCompileStats(environmentId: String): List[Date] = {
    ???
    /*
    DB.withConnection {
      implicit connection =>

        val gcal = new GregorianCalendar
        val today = new GregorianCalendar(gcal.get(Calendar.YEAR), gcal.get(Calendar.MONTH), gcal.get(Calendar.DATE))

        val startTimes = SQL(
          """
          select startTime from request_data
          where environmentId={environmentId} and status=200 and isStats = 'false' and isMock = 'false' and startTime < {today}
          """
        ).on('environmentId -> environmentId, 'today -> today.getTime)
          .as(get[Date]("startTime") *).toList

        val uniqueStartTimePerDay: Set[Date] = Set()
        startTimes.foreach {
          (t) =>
            gcal.setTimeInMillis(t.getTime)
            val ccal = new GregorianCalendar(gcal.get(Calendar.YEAR), gcal.get(Calendar.MONTH), gcal.get(Calendar.DATE))
            uniqueStartTimePerDay += ccal.getTime
        }
        uniqueStartTimePerDay.toList
    }
    */
  }

  def load(id: Long): RequestData = {
    ???
    /*DB.withConnection {
      implicit connection =>
        SQL("select id, sender, serviceAction, environmentId, serviceId, request, requestHeaders, contentType, requestCall from request_data where id= {id}")
          .on('id -> id).as(RequestData.forReplay.single)
    }*/
  }

  def loadRequest(id: Long): Option[String] = {
    ???
    /*DB.withConnection {
      implicit connection =>
        val request = SQL("select request, contentType from request_data where id= {id}").on('id -> id).as((bytes("request") ~ get[String]("contentType")).singleOpt)
        if (!request.get._1.equals(None)) {
          (Some(uncompressString(request.get._1)), request.get._2)
        } else {
          (None, request.get._2)
        }
    }*/
  }

  def loadForREST(id: Long): RequestData = {
    ???
    // TO KEEP ?

    /*DB.withConnection {
      implicit connection =>
        SQL("select id, sender, serviceAction, environmentId, serviceId, request, requestHeaders, requestCall from request_data where id= {id}")
          .on('id -> id).as(RequestData.forRESTReplay.single)
    }*/
  }


  def loadResponse(id: Long): (Option[String], String) = {
    ???
    /*
    DB.withConnection {

      implicit connection =>
        val response = SQL("select response, contentType from request_data where id = {id}").on('id -> id).as((bytes("response") ~ get[String]("contentType")).singleOpt)
        if (!(response.get._1.equals(None))) {
          (Some(uncompressString(response.get._1)), response.get._2)
        } else {
          (None, response.get._2)
        }
    }*/
  }

  /* TO_DELETE
  private def sqlAndStatus(statusIn : String) : String = {

    var status = ""
    if (statusIn != "all") {
      if (statusIn.startsWith("NOT_")) {
        status = " and status != " + statusIn.substring(4)
      } else {
        status = " and status = " + statusIn
      }
    }
    status
  }

  private def sqlAndEnvironnement(environmentIn: String): String = {
    var environment = ""

    // search by name
    if (environmentIn != "all" && Environment.optionsAll.exists(t => t._2 == environmentIn))
      environment = " and request_data.environmentId = " + Environment.optionsAll.find(t => t._2 == environmentIn).get._1

    // search by id
    if (environment == "" && Environment.optionsAll.exists(t => t._1 == environmentIn))
      environment = " and request_data.environmentId = " + Environment.optionsAll.find(t => t._1 == environmentIn).get._1

    environment
  }*/

  /*private def sqlFromAndGroup(groupName: String, environmentIn: String): (String, String) = {
    var group = ""
    var from = ""

    // search by name
    if (environmentIn == "all" && groupName != "all" && Group.options.exists(t => t._2 == groupName)) {
      group += " and request_data.environmentId = environment.id"
      group += " and environment.groupId = " + Group.options.find(t => t._2 == groupName).get._1
      from = ", environment "
    }

    (from, group)
  }*/

  // use by Json : from scala to json
  /*implicit object RequestDataWrites extends Writes[RequestData] {

    def writes(o: RequestData): JsValue = {
      val id = if (o.id != null) o.id.toString else "-1"
      JsObject(
        List(
          "id" -> JsString(id),
          "purged" -> JsString(o.purged.toString),
          "status" -> JsString(o.status.toString),
          "env" -> JsString(Environment.optionsAll.find(t => t._1 == o.environmentId.toString).get._2),
          "sender" -> JsString(o.sender),
          "service" -> JsString(o.serviceId.toString),
          "contentType" -> JsString(o.contentType),
          "serviceAction" -> JsString(o.serviceAction),
          "startTime" -> JsString(UtilDate.getDateFormatees(o.startTime)),
          "time" -> JsString(o.timeInMillis.toString),
          "isMock" -> JsString(o.isMock.toString)))
    }
  }
  */

  /*private def explainPlan(sql: String, params: (Any, anorm.ParameterValue[_])*)(implicit connection: Connection) {
    val plan = SQL("explain plan for " + sql).on(params: _*).resultSet()
    while (plan.next) {
      Logger.debug(plan.getString(1))
    }
  }*/

  /**
   * Upload a csvLine => insert requestDataStat.
   *
   * @param csvLine line in csv file
   * @return nothing
   */
  def upload(csvLine: String) = {
    ???
    /*
    val dataCsv = csvLine.split(";")

    if (dataCsv.size != csvTitle.size)
      throw new Exception("Please check csvFile, " + csvTitle.size + " fields required")

    if (dataCsv(csvTitle.get("key").get) != csvKey) {
      Logger.info("Line does not match with " + csvKey + " of csvLine - ignored")
    } else {
      val environmentName = dataCsv(csvTitle.get("environmentName").get).trim
      val e = Environment.findByName(environmentName)

      e.map {
        environment =>
          insertStats(environment.id, dataCsv(csvTitle.get("serviceAction").get).trim, UtilDate.parse(dataCsv(csvTitle.get("startTime").get).trim), dataCsv(csvTitle.get("timeInMillis").get).toLong)
      }.getOrElse {
        Logger.warn("Warning : Environment " + environmentName + " unknown")
        throw new Exception("Warning : Environment " + environmentName + " already exist")
      }
    }
    */
  }

  /**
   * Compress a string with Gzip
   *
   * @param inputString String to compress
   * @return compressed string
   */
  def compressString(inputString: String): Array[Byte] = {
    try {
      val os = new ByteArrayOutputStream(inputString.length())
      val gos = new GZIPOutputStream(os)
      gos.write(inputString.getBytes(Charset.forName("utf-8")))
      gos.close()
      val compressed = os.toByteArray()
      os.close()
      compressed
    } catch {
      case e: Exception => Logger.error("compressString : Error during compress string")
        inputString.getBytes(Charset.forName("utf-8"))
    }
  }

  /**
   * Uncompress a String with gzip
   *
   * @param compressed compressed String
   * @return clear String
   */
  def uncompressString(compressed: Array[Byte]): String = {
    try {
      val BUFFER_SIZE = 1000
      val is = new ByteArrayInputStream(compressed)
      val gis = new GZIPInputStream(is, BUFFER_SIZE)
      val output = new StringBuilder()
      val data = new Array[Byte](BUFFER_SIZE)
      var ok = true
      while (ok) {
        val bytesRead = gis.read(data)
        ok = bytesRead != -1
        if (ok) output.append(new String(data, 0, bytesRead))
      }
      gis.close()
      is.close()
      output.toString()
    } catch {
      case e: Exception => Logger.error("decompressString : Error during uncompress string:" + e.getStackTraceString)
        new String(compressed, 0, compressed.length, Charset.forName("utf-8"))
    }
  }
}
