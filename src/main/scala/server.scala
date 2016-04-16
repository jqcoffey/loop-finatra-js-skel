package org.your

import java.sql.{Timestamp, Types}
import java.text.SimpleDateFormat
import java.util.TimeZone
import javax.sql.DataSource
import java.sql.Date

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import org.h2.tools.Server
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.util.Try

object AppServer extends HttpServer {

  val conf = ServerConfig()
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  Scaffolding.build(conf.dataSource)
  val h2Server = Server.createWebServer("-webAllowOthers", "-webPort", "8082").start()

  val dataSvc = new DataService(conf.queries)

  override def configureHttp(router: HttpRouter) {
    router
      .filter[CommonFilters]
      .add(new AppController(dataSvc))
  }

}

case class ServerConfig(dataSource: DataSource, queries: Seq[Query])

case class DataSourceConfig(url: String, driver: Class[_], user: String, password: String)

object ServerConfig {

  def apply(): ServerConfig = {

    val dataSource = buildDataSource(
      DataSourceConfig(
        url = "jdbc:h2:mem:sap;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE",
        driver = classOf[org.h2.Driver],
        user = "sa",
        password = ""
      )
    )

    ServerConfig(
      dataSource = dataSource,
      queries = Query(
        name = "rates",
        db = dataSource,
        sql = "select * from rates"
      ) :: Query(
        name = "milk",
        db = dataSource,
        sql = "select * from milk"
      ) :: Nil
    )
  }

  def buildDataSource(config: DataSourceConfig): DataSource = {
    val ds = new ComboPooledDataSource
    try {
      ds.setDriverClass(config.driver.getName)
    } catch {
      case e: Exception =>
        throw new RuntimeException("Unable to initialize datasource", e)
    }
    ds.setJdbcUrl(config.url)
    ds.setUser(config.user)
    ds.setPassword(config.password)
    ds
  }

}

object Scaffolding {

  val log = LoggerFactory.getLogger("scaffolding")

  val ratesFormatter = new SimpleDateFormat("MMM-yyyy")
  val milkFormatter = new SimpleDateFormat("yyyy-MM")

  def build(ds: DataSource): Unit = {
    val conn = ds.getConnection()
    conn.createStatement().execute(
      "create table rates (month date, us_rate double, jp_rate double)"
    )

    val ratesInsert = conn.prepareStatement(
      "insert into rates (month, us_rate, jp_rate) values (?,?,?)"
    )
    val ratesStream = getClass.getResourceAsStream("/rates-US-JP.csv")
    Source.fromInputStream(ratesStream, "utf-8").getLines().foreach { line =>

      val data = line.split(",")

      ratesInsert.setDate(1, new Date(ratesFormatter.parse(data(0)).getTime))
      ratesInsert.setDouble(2, data(1).toDouble)
      ratesInsert.setDouble(3, data(2).toDouble)
      ratesInsert.executeUpdate
    }

    conn.createStatement().execute(
      "create table milk (month date, pounds_produced int)"
    )

    val milkInsert = conn.prepareStatement(
      "insert into milk (month, pounds_produced) values (?,?)"
    )
    val milkStream = getClass.getResourceAsStream("/monthly-milk-production-pounds-p.csv")
    Source.fromInputStream(milkStream, "utf-8").getLines().foreach { line =>

      val data = line.split(",")

      milkInsert.setDate(1, new Date(milkFormatter.parse(data(0)).getTime))
      milkInsert.setInt(2, data(1).toInt)
      milkInsert.executeUpdate
    }

    milkInsert.close()
    ratesInsert.close()
    conn.close()
  }

}
