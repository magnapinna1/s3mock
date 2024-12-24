# S3 mock library for Java/Scala

s3mock is a web service implementing AWS S3 API, which can be used for local testing of your code using S3
but without hitting real S3 endpoints.

**NOTE**: This library is a fork of https://github.com/ckipp01/s3mock. I just compiled it with Scala 2.12. There will not
be active development on it... I just needed it for work.

### NOTE
The old findify version of this repo would automatically handle HEAD requests. It seems this has been disabled by default with the switch from Akka to Pekko.
To restore the old behavior, add the config option `pekko.http.server.transparent-head-requests = on` to `resources/application.conf`

Implemented API methods:
* list buckets
* list objects (all & by prefix)
* create bucket
* delete bucket
* put object (via PUT, POST, multipart and chunked uploads are also supported)
* copy object
* get object
* delete object
* batch delete

Not supported features (these might be implemented later):
* authentication: s3proxy will accept any credentials without validity and signature checking
* bucket policy, ACL, versioning
* object ACL
* posix-incompatible key structure with file-based provider, for example keys `/some.dir/file.txt` and `/some.dir` in the same bucket

## Installation

s3mock package is available for Scala 2.12 and tested on Java 11/17. To install using sbt, add these
 statements to your `build.sbt`:

```
libraryDependencies += "io.github.magnapinna1" %% "s3mock" % "<version>" % Test
```

## Usage

Just point your s3 client to a localhost, enable path-style access, and it should work out of the box.

There are two working modes for s3mock:
* File-based: it will map a local directory as a collection of s3 buckets. This mode can be useful when you need to have a bucket with some pre-loaded data (and too lazy to re-upload everything on each run).
* In-memory: keep everything in RAM. All the data you've uploaded to s3mock will be wiped completely on shutdown. 

Java:
```java
    import com.amazonaws.auth.AWSStaticCredentialsProvider;
    import com.amazonaws.auth.AnonymousAWSCredentials;
    import com.amazonaws.client.builder.AwsClientBuilder;
    import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
    import com.amazonaws.services.s3.AmazonS3;
    import com.amazonaws.services.s3.AmazonS3Builder;
    import com.amazonaws.services.s3.AmazonS3Client;
    import com.amazonaws.services.s3.AmazonS3ClientBuilder;
    import io.magnapinna.s3mock.S3Mock;
    
    /*
     S3Mock.create(8001, "/tmp/s3");
     */
    S3Mock api = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();
    api.start();
            
    /* AWS S3 client setup.
     *  withPathStyleAccessEnabled(true) trick is required to overcome S3 default 
     *  DNS-based bucket access scheme
     *  resulting in attempts to connect to addresses like "bucketname.localhost"
     *  which requires specific DNS setup.
     */
    EndpointConfiguration endpoint = new EndpointConfiguration("http://localhost:8001", "us-west-2");
    AmazonS3Client client = AmazonS3ClientBuilder
      .standard()
      .withPathStyleAccessEnabled(true)  
      .withEndpointConfiguration(endpoint)
      .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))     
      .build();

    client.createBucket("testbucket");
    client.putObject("testbucket", "file/name", "contents");
    api.shutdown(); // kills the underlying actor system. Use api.stop() to just unbind the port.
```

Scala with AWS S3 SDK:
```scala
    import com.amazonaws.auth.AWSStaticCredentialsProvider
    import com.amazonaws.auth.AnonymousAWSCredentials
    import com.amazonaws.client.builder.AwsClientBuilder
    import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
    import com.amazonaws.services.s3.AmazonS3
    import com.amazonaws.services.s3.AmazonS3Builder
    import com.amazonaws.services.s3.AmazonS3Client
    import com.amazonaws.services.s3.AmazonS3ClientBuilder
    import io.magnapinna.s3mock.S3Mock

    
    /** Create and start S3 API mock. */
    val api = S3Mock(port = 8001, dir = "/tmp/s3")
    api.start

    /* AWS S3 client setup.
     *  withPathStyleAccessEnabled(true) trick is required to overcome S3 default 
     *  DNS-based bucket access scheme
     *  resulting in attempts to connect to addresses like "bucketname.localhost"
     *  which requires specific DNS setup.
     */
    val endpoint = new EndpointConfiguration("http://localhost:8001", "us-west-2")
    val client = AmazonS3ClientBuilder
      .standard
      .withPathStyleAccessEnabled(true)  
      .withEndpointConfiguration(endpoint)
      .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))     
      .build

    /** Use it as usual. */
    client.createBucket("foo")
    client.putObject("foo", "bar", "baz")
    api.shutdown() // this one terminates the actor system. Use api.stop() to just unbind the service without messing with the ActorSystem
```

Scala with Pekko Connectors Kafka:
```scala
    import org.apache.pekko.ActorSystem
    import org.apache.pekko.ActorMaterializer
    import org.apache.pekko.stream.connectors.s3.scaladsl.S3Client
    import org.apache.pekko.stream.scaladsl.Sink
    import com.typesafe.config.ConfigFactory
    import scala.collection.JavaConverters._

    val config = ConfigFactory.parseMap(Map(
      "alpakka.s3.proxy.host" -> "localhost",
      "alpakka.s3.proxy.port" -> 8001,
      "alpakka.s3.proxy.secure" -> false,
      "alpakka.s3.path-style-access" -> true,
      "alpakka.s3.aws.credentials.provider" -> "static",
      "alpakka.s3.aws.credentials.access-key-id" -> "foo",
      "alpakka.s3.aws.credentials.secret-access-key" -> "bar",
      "alpakka.s3.aws.region.provider" -> "static",
      "alpakka.s3.aws.region.default-region" -> "us-east-1"      
    ).asJava)
    implicit val system = ActorSystem.create("test", config)
    implicit val mat = ActorMaterializer()
    import system.dispatcher
    val s3a = S3Client()
    val contents = s3a.download("bucket", "key")._1.runWith(Sink.reduce[ByteString](_ ++ _)).map(_.utf8String)
      
```

### NOTE
The old findify version of this repo would automatically handle HEAD requests. It seems this has been disabled by default with the switch from Akka to Pekko.
To restore the old behavior, add the config option `pekko.http.server.transparent-head-requests = on` to `resources/application.conf`

### To publish

Increment version in build.sbt
Install GPG and create Key as documented here https://central.sonatype.org/publish/requirements/#sign-files-with-gpgpgp
Create file `~/.sbt/<version>/sonatype.sbt` with the following contents retrieved from central.sonatype.com

credentials += Credentials("Sonatype Nexus Repository Manager",
"central.sonatype.com",
"username",
"password")

Run `publishSigned`
Run `sonatypeCentralUpload`

Login to central.sonatype.com and verify the validation status. Click the "publish" button if it succeeded.