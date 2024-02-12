package io.kipp.s3mock.route

import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.LazyLogging
import io.kipp.s3mock.error.{InternalErrorException, NoSuchBucketException}
import io.kipp.s3mock.provider.Provider
import io.kipp.s3mock.request.CompleteMultipartUpload

import scala.util.{Failure, Success, Try}

case class PutObjectMultipartComplete()(implicit provider: Provider)
    extends LazyLogging {
  def route(bucket: String, path: String) = post {
    parameter('uploadId) { uploadId =>
      entity(as[String]) { xml =>
        complete {
          logger.info(
            s"multipart upload completed for $bucket/$path, id = $uploadId"
          )
          val request =
            CompleteMultipartUpload(scala.xml.XML.loadString(xml).head)
          Try(
            provider.putObjectMultipartComplete(bucket, path, uploadId, request)
          ) match {
            case Success(response) =>
              HttpResponse(
                StatusCodes.OK,
                entity = HttpEntity(
                  ContentType(
                    MediaTypes.`application/xml`,
                    HttpCharsets.`UTF-8`
                  ),
                  response.toXML.toString()
                )
              )
            case Failure(e: NoSuchBucketException) =>
              HttpResponse(
                StatusCodes.NotFound,
                entity = e.toXML.toString()
              )
            case Failure(t) =>
              HttpResponse(
                StatusCodes.InternalServerError,
                entity = InternalErrorException(t).toXML.toString()
              )
          }
        }
      }
    }
  }
}
