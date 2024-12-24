package io.magnapinna.s3mock.awscli

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.stream.ActorMaterializer
import io.magnapinna.s3mock.S3MockTest

/** Created by shutty on 8/28/16.
  */
trait AWSCliTest extends S3MockTest {
  implicit val system: ActorSystem =
    ActorSystem.create("awscli")
  implicit val mat: ActorMaterializer =
    ActorMaterializer()
  val http = Http(system)
}
