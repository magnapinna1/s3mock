package io.magnapinna.s3mock.route

import org.apache.pekko.http.scaladsl.model.{HttpResponse, StatusCodes}
import org.apache.pekko.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.LazyLogging
import io.magnapinna.s3mock.error.NoSuchKeyException
import io.magnapinna.s3mock.provider.Provider

import scala.util.{Failure, Success, Try}

case class DeleteObject()(implicit provider: Provider) extends LazyLogging {
  def route(bucket: String, path: String) = delete {
    complete {
      Try(provider.deleteObject(bucket, path)) match {
        case Success(_) =>
          logger.info(s"deleted object $bucket/$path")
          HttpResponse(StatusCodes.NoContent)
        case Failure(NoSuchKeyException(_, _)) =>
          logger.info(s"cannot delete object $bucket/$path: no such key")
          HttpResponse(StatusCodes.NotFound)
        case Failure(ex) =>
          logger.error(s"cannot delete object $bucket/$path", ex)
          HttpResponse(StatusCodes.NotFound)
      }

    }
  }
}
