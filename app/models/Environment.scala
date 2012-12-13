package models

import play.api.db._
import play.api.Play.current
import play.api.cache._
import play.api._

import anorm._
import anorm.SqlParser._
import java.util.GregorianCalendar

case class Environment(id: Pk[Long],
  name: String,
  hourRecordXmlDataMin: Int = 6,
  hourRecordXmlDataMax: Int = 22,
  nbDayKeepXmlData: Int = 5)

object Environment {

  private val keyCacheAllOptions = "environment-options"
  private val keyCacheById = "environment-all"

  /**
   * Parse a Environment from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("id") ~
      get[String]("name") ~
      get[Int]("hourRecordXmlDataMin") ~
      get[Int]("hourRecordXmlDataMax") ~
      get[Int]("nbDayKeepXmlData") map {
        case id ~ name ~ hourRecordXmlDataMin ~ hourRecordXmlDataMax ~ nbDayKeepXmlData => Environment(id, name, hourRecordXmlDataMin, hourRecordXmlDataMax, nbDayKeepXmlData)
      }
  }

  /**
   * Construct the Map[String,String] needed to fill a select options set.
   */
  def options: Seq[(String, String)] = DB.withConnection {
    implicit connection =>
      val envs = Cache.getOrElse[Seq[(String, String)]](keyCacheAllOptions) {
        Logger.debug("Environments not found in cache: loading from db")
        SQL("select * from environment order by name").as(Environment.simple *).map(c => c.id.toString -> c.name)
      }

      val sortedEnvs = envs.sortWith { (a, b) =>
        val pattern = """^(.+?)([0-9]+)$""".r

        val matchA = pattern.findAllIn(a._2)
        val matchB = pattern.findAllIn(b._2)

        if (matchA.hasNext && matchB.hasNext) {
          // both names match the regex: compare name then number
          val nameA = matchA.group(1)
          val numberA = matchA.group(2)
          val nameB = matchB.group(1)
          val numberB = matchB.group(2)
          if (nameA != nameB) {
            nameA < nameB
          } else {
            numberA.toInt <= numberB.toInt
          }

        } else if (matchA.hasNext) {
          val nameA = matchA.group(1)
          // only a matches the regex
          nameA < b._2

        } else if (matchB.hasNext) {
          val nameB = matchB.group(1)
          // only b matches the regex
          a._2 < nameB

        } else {
          // none matches the regex
          a._2 < b._2
        }
      }

      sortedEnvs
  }

  /**
   * Retrieve an Environment from id.
   */
  def findById(id: Long): Option[Environment] = {
    DB.withConnection {
      implicit connection =>
        Cache.getOrElse[Option[Environment]](keyCacheById + id) {
          SQL("select * from environment where id = {id}").on(
            'id -> id).as(Environment.simple.singleOpt)
        }
    }
  }

  /**
   * Retrieve an Environment from name.
   */
  def findByName(name: String): Option[Environment] = DB.withConnection {
    implicit connection =>
      // FIXME : add key to clearCache
      //Cache.getOrElse[Option[Environment]](keyCacheByName + name) {
      SQL("select * from environment where name = {name}").on(
        'name -> name).as(Environment.simple.singleOpt)
    //}
  }

  /**
   * Insert a new environment.
   *
   * @param environment The environment values.
   */
  def insert(environment: Environment) = {
    clearCache
    DB.withConnection {
      implicit connection =>
        SQL(
          """
            insert into environment values (
              (select next value for environment_seq), 
              {name}, {hourRecordXmlDataMin},
              {hourRecordXmlDataMax}, {nbDayKeepXmlData}
            )
          """).on(
            'name -> environment.name,
            'hourRecordXmlDataMin -> environment.hourRecordXmlDataMin,
            'hourRecordXmlDataMax -> environment.hourRecordXmlDataMax,
            'nbDayKeepXmlData -> environment.nbDayKeepXmlData).executeUpdate()
    }
  }

  /**
   * Update a environment.
   *
   * @param id The environment id
   * @param environment The environment values.
   */
  def update(id: Long, environment: Environment) = {
    clearCache
    Cache.remove(keyCacheById + id)
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update environment
          set name = {name},
          hourRecordXmlDataMin = {hourRecordXmlDataMin},
          hourRecordXmlDataMax = {hourRecordXmlDataMax},
          nbDayKeepXmlData = {nbDayKeepXmlData}
          where id = {id}
          """).on(
            'id -> id,
            'name -> environment.name,
            'hourRecordXmlDataMin -> environment.hourRecordXmlDataMin,
            'hourRecordXmlDataMax -> environment.hourRecordXmlDataMax,
            'nbDayKeepXmlData -> environment.nbDayKeepXmlData)
          .executeUpdate()
    }
  }

  /**
   * Delete a environment.
   *
   * @param id Id of the environment to delete.
   */
  def delete(id: Long) = {
    clearCache
    Cache.remove(keyCacheById + id)
    DB.withConnection {
      implicit connection =>
        SQL("delete from environment where id = {id}").on('id -> id).executeUpdate()
    }
  }

  def clearCache() {
    Cache.remove(keyCacheAllOptions)
  }

  /**
   * Return a page of (Environment).
   *
   * @param page Page to display
   * @param pageSize Number of environments per page
   * @param orderBy Environment property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Environment)] = {

    val offest = pageSize * page

    DB.withConnection {
      implicit connection =>

        val environments = SQL(
          """
          select * from environment
          where environment.name like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
          """).on(
            'pageSize -> pageSize,
            'offset -> offest,
            'filter -> filter,
            'orderBy -> orderBy).as(Environment.simple *)

        val totalRows = SQL(
          """
          select count(*) from environment
          where environment.name like {filter}
          """).on(
            'filter -> filter).as(scalar[Long].single)

        Page(environments, page, offest, totalRows)

    }
  }

  def purgeXmlData() = {
    Logger.info("Purging XML data...")
    val minDate = UtilDate.getDate("all").getTime

    var purgedRequests = 0
    Environment.options.foreach { (e) =>
      Logger.debug("Purge env:" + e._1)
      val env = Environment.findById(e._1.toInt).get

      if (env.hourRecordXmlDataMin < env.hourRecordXmlDataMax) {
        val maxDate = new GregorianCalendar
        maxDate.setTimeInMillis(maxDate.getTimeInMillis - UtilDate.v1d * env.nbDayKeepXmlData)

        Logger.debug("env.name: " + env.name + " NbDaysKeep: " + env.nbDayKeepXmlData + " MinDate:" + minDate + " MaxDate:" + maxDate.getTime)
        val user = "Soapower Akka Scheduler (keep xml data for " + env.nbDayKeepXmlData + " days for this env " + env.name + ")"
        purgedRequests += RequestData.deleteRequestResponse(env.name, minDate, maxDate.getTime, user)
      } else {
        Logger.error("Invalid min / max hours for environment " + env.name)
      }
    }

    Logger.info("Purging XML data: done (" + purgedRequests + " requests purged)")
  }

}
