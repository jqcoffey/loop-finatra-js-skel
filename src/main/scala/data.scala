package org.your

import java.sql.{PreparedStatement, Connection, ResultSet}
import javax.sql.DataSource

import scala.collection.mutable.ListBuffer
import scala.util.Try

class DataService(queries: Seq[Query]) {

  private val cache: Map[String, Query] = queries.map(q => q.name -> q).toMap

  def query(name: String, params: Seq[Any]): Option[QueryResult] = cache.get(name).map { q =>
    q.exec(withParams = params)
  }

}

case class Query(name: String, db: DataSource, sql: String) {

  def exec(withParams: Seq[Any]): QueryResult = {
    var conn: Connection = null
    var stmt: PreparedStatement = null
    var rs: ResultSet = null

    val results = Try {
      conn = db.getConnection
      stmt = conn.prepareStatement(sql)

      withParams.zipWithIndex.foreach { case (param, i) =>
        stmt.setObject(i + 1, param)
      }

      rs = stmt.executeQuery()

      val md = rs.getMetaData
      val colCount = md.getColumnCount
      val colIdx = 1 to colCount
      val cols = colIdx.map { i => md.getColumnLabel(i) }

      val buffer = ListBuffer.empty[Seq[Any]]
      while (rs.next()) {
        buffer += colIdx.map(i => rs.getObject(i))
      }

      QueryResult(cols, buffer)

    }.getOrElse(QueryResult(Nil, Nil))

    if (rs != null && !rs.isClosed)
      rs.close

    if (stmt != null && !stmt.isClosed)
      stmt.close

    if (conn != null && !conn.isClosed)
      conn.close

    results
  }

}

case class QueryResult(columns: Seq[String], data: Iterable[Seq[Any]]) {

  def zipped(): Iterable[Map[String, Any]] = {
    val colIdx = columns.zipWithIndex.map { case (name, idx) => name -> idx }.toMap
    data.map { row =>
      colIdx.map { case (name, idx) =>
        name -> row(idx)
      }
    }
  }

}
