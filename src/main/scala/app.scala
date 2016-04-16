package org.your

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class AppController(dataSvc: DataService) extends Controller {

  get("/ping") { request: Request =>
    val version = Option(getClass.getResourceAsStream("/version")).map(io.Source.fromInputStream).map(_.mkString).getOrElse("local")
    response.ok.plain(s"version $version")
  }

  post("/query") { r: QueryRequest =>
    logger.debug(s"have query request to process: $r")
    dataSvc.query(r.name, r.params)
  }

  get("/:*") { request: Request =>
    response.ok.fileOrIndex(
      request.params("*"),
      "index.html"
    )
  }

}

case class QueryRequest(name: String, params: Seq[Any])
