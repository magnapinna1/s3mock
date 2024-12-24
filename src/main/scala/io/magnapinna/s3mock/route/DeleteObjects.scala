package io.magnapinna.s3mock.route

import org.apache.pekko.http.scaladsl.model.{HttpResponse, StatusCodes, Uri}
import org.apache.pekko.http.scaladsl.server.Directives.{path, _}
import com.typesafe.scalalogging.LazyLogging
import io.magnapinna.s3mock.error.NoSuchKeyException
import io.magnapinna.s3mock.provider.Provider
import io.magnapinna.s3mock.request.DeleteObjectsRequest
import io.magnapinna.s3mock.response.DeleteObjectsResponse

import scala.util.{Failure, Success, Try}

case class DeleteObjects()(implicit provider: Provider) extends LazyLogging {
  def route(bucket: String) = post {
    parameter('delete) { d =>
      entity(as[String]) { xml =>
        {
          complete {
            val request =
              DeleteObjectsRequest(scala.xml.XML.loadString(xml).head)
            val response =
              request.objects.foldLeft(DeleteObjectsResponse(Nil, Nil))(
                (res, rawPath) => {
                  val path = Uri.Path(rawPath).toString // URL-encoded
                  Try(provider.deleteObject(bucket, path)) match {
                    case Success(_) =>
                      logger.info(s"deleted object $bucket/$path")
                      res.copy(deleted = path +: res.deleted)
                    case Failure(NoSuchKeyException(_, _)) =>
                      logger.info(
                        s"cannot delete object $bucket/$path: no such key"
                      )
                      res.copy(error = path +: res.error)
                    case Failure(ex) =>
                      logger.error(s"cannot delete object $bucket/$path", ex)
                      res.copy(error = path +: res.error)
                  }
                }
              )
            val xmlResponse = response.toXML.toString()
            HttpResponse(StatusCodes.OK, entity = xmlResponse)
          }
        }
      }
    }
  }
}
