package file.upload.micronaut.endpoints;

import file.upload.micronaut.MyProperties;
import file.upload.repository.FileServices;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller( "/upload" )
public class UploadEndpoint {

  private static Logger log = LoggerFactory.getLogger( UploadEndpoint.class );

  private MyProperties properties;
  private FileServices file_services;

  @Inject
  public UploadEndpoint( MyProperties properties , FileServices file_services ) {
    this.properties = properties;
    this.file_services = file_services;
  }

  //Try HttpResponse upload(StreamingFileUpload file, Map<String, Object> metadata) { without @Body.

  @Post( value = "/", consumes = MediaType.MULTIPART_FORM_DATA )
  public HttpResponse<String> uploadCompleted( CompletedFileUpload upload_file ) {
    log.info( "Form Name : {}" , upload_file.getName( ) );
    log.info( "File Name : {}" , upload_file.getFilename( ) );

    long start = System.currentTimeMillis( );

    try {
      File tempFile = properties.getDestination( upload_file.getFilename( ) ).toFile( );
      tempFile.createNewFile( );
//      File tempFile = File.createTempFile(upload_file.getFilename(), "temp");

      Path path = Paths.get( tempFile.getAbsolutePath( ) );
      Files.write( path , upload_file.getBytes( ) );

      float duration = ( System.currentTimeMillis( ) - start ) / 1000;
      float size = tempFile.length( ) / 1_048_576;
      float throughput = size / duration;
      log.info( "Full size  : " + size + " mb" );
      log.info( "Duration   : " + duration + " s" );
      log.info( "Throughput : " + throughput + " mb/s" );
      log.info( "Final file : " + tempFile.getAbsolutePath( ) );

      return HttpResponse.ok( "Uploaded" );
    }
    catch ( IOException exception ) {
      return HttpResponse.badRequest( "Upload Failed" );
    }
  }

//  @Post( value = "/", consumes = MediaType.MULTIPART_FORM_DATA )
//  public Single<HttpResponse<String>> upload( StreamingFileUpload upload_file ) {
//
//    log.info( "Form Name : {}" , upload_file.getName( ) );
//    log.info( "File Name : {}" , upload_file.getFilename( ) );
//
//    long start = System.currentTimeMillis( );
//
//    File tempFile;
//    try {
//      tempFile = File.createTempFile( upload_file.getFilename( ) , "temp" );
//    }
//    catch ( IOException e ) {
//      return Single.error( e );
//    }
//    Publisher<Boolean> uploadPublisher = upload_file.transferTo( tempFile );
//    return Single.fromPublisher( uploadPublisher )
//        .map( success -> {
//          if ( success ) {
//            float duration = ( System.currentTimeMillis( ) - start ) / 1000;
//            float size = tempFile.length( ) / 1024 / 1024;
//            float throughput = size / duration;
//            log.info( "Full size  : " + size + " mb" );
//            log.info( "Duration   : " + duration + " s" );
//            log.info( "Throughput : " + throughput + " mb/s" );
//            log.info( "Final file : " + tempFile.getAbsolutePath() );
//
//            return HttpResponse.ok( "Uploaded" );
//          }
//          else {
//            return HttpResponse.<String>status( HttpStatus.CONFLICT )
//                .body( "Upload Failed" );
//          }
//        } );
//  }

//  @Post( "/A" )
//  @Consumes( MediaType.MULTIPART_FORM_DATA )
////  public Single<HttpResponse<String>> stream( StreamingFileUpload upload_file, Map<String, Object> metadata ) {
//  public Single<HttpResponse<String>> stream( StreamingFileUpload upload_file ) {
//
//    log.info( "Form Name : {}" , upload_file.getName( ) );
//    log.info( "File Name : {}" , upload_file.getFilename( ) );
//
//    long start = System.currentTimeMillis( );
//
//    String final_file = properties.getDestination( upload_file.getFilename( ) ).toFile( );
//    final_file.createNewFile();
//
//    Publisher<Boolean> uploadPublisher = upload_file.transferTo( final_file );
//    return Single.fromPublisher( uploadPublisher )
//        .map( success -> {
//          if ( success ) {
//            float duration = ( System.currentTimeMillis( ) - start ) / 1000;
//            float size = final_file.length( ) / 1024 / 1024;
//            float throughput = size / duration;
//
//            log.info( "Full size  : " + size + " mb" );
//            log.info( "Duration   : " + duration + " s" );
//            log.info( "Throughput : " + throughput + " mb/s" );
//            log.info( "Final file : " + final_file );
//            return HttpResponse.ok( final_file );
//          }
//          else {
//            return HttpResponse.<String>status( HttpStatus.CONFLICT ).body( final_file );
//          }
//        } );
//  }
//
//  @Post( "/1" )
//  @Consumes( MediaType.MULTIPART_FORM_DATA )
////  public Single<HttpResponse<String>> stream( StreamingFileUpload upload_file, Map<String, Object> metadata ) {
//  public HttpResponse<String> stream1( StreamingFileUpload upload_file ) throws IOException {
//
//    long start = System.currentTimeMillis( );
//
//    File final_file = properties.getDestination( upload_file.getFilename( ) ).toFile( );
//    Publisher<Boolean> uploadPublisher = upload_file.transferTo( final_file );
//
//    float duration = ( System.currentTimeMillis( ) - start ) / 1000;
//    float size = final_file.length( ) / 1024 / 1024;
//    float throughput = size / duration;
//
//    log.info( "Full size  : " + size + " mb" );
//    log.info( "Duration   : " + duration + " s" );
//    log.info( "Throughput : " + throughput + " mb/s" );
//    log.info( "Final file : " + final_file );
//    return HttpResponse.ok( final_file.getAbsolutePath( ) );
//  }
//
//  @Post( value = "/2", consumes = MediaType.MULTIPART_FORM_DATA )
//  public HttpResponse<String> uploadBytes( byte[] upload_file ) {
//    long start = System.currentTimeMillis( );
//    try {
//      File final_file = properties.getDestination( "ok.tmp" ).toFile( );
//      Path path = Paths.get( final_file.getAbsolutePath( ) );
//      Files.write( path , upload_file );
//
//      float duration = ( System.currentTimeMillis( ) - start ) / 1000;
//      float size = final_file.length( ) / 1024 / 1024;
//      float throughput = size / duration;
//      log.info( "Full size  : " + size + " mb" );
//      log.info( "Duration   : " + duration + " s" );
//      log.info( "Throughput : " + throughput + " mb/s" );
//      log.info( "Final file : " + final_file );
//      return HttpResponse.ok( final_file.getAbsolutePath( ) );
//    }
//    catch ( IOException exception ) {
//      return HttpResponse.badRequest( "Upload Failed" );
//    }
//  }
//
//  @Post( value = "/3", consumes = MediaType.MULTIPART_FORM_DATA )
//  public Publisher<MutableHttpResponse<?>> receiveFileUpload( StreamingFileUpload upload_file , String title ) {
//    long size = upload_file.getDefinedSize( );
//    return Flowable.fromPublisher( upload_file.transferTo( title + ".json" ) )
//        .map( success -> success ? HttpResponse.ok( "Uploaded " + size ) :
//            HttpResponse.status( HttpStatus.INTERNAL_SERVER_ERROR , "Something bad happened" ) ).onErrorReturnItem(
// HttpResponse.status( HttpStatus.INTERNAL_SERVER_ERROR , "Something bad happened" ) );
//  }
//
//  @Post( value = "/4", consumes = MediaType.MULTIPART_FORM_DATA )
//  public Single<HttpResponse> receiveMultipleStreaming(
//      Flowable<StreamingFileUpload> upload_file ) {
//    return upload_file.subscribeOn( Schedulers.io( ) ).flatMap( ( StreamingFileUpload upload ) -> {
//      return Flowable.fromPublisher( upload )
//          .map( ( pd ) -> {
//            try {
//              return pd.getBytes( );
//            }
//            catch ( IOException e ) {
//              throw Exceptions.propagate( e );
//            }
//          } );
//    } ).collect( LongAdder::new , ( adder , bytes ) -> adder.add( ( long ) bytes.length ) )
//        .map( ( adder ) -> {
//          return HttpResponse.ok( adder.longValue( ) );
//        } );
//  }
//  @Consumes(MediaType.MULTIPART_FORM_DATA)
//  @Post("/3")
//  public HttpResponse upload(StreamingFileUpload file) {
//    fileRepository.upload(file.getName(), file);
//    return HttpResponse.ok();
//  }
//
//  public void upload(String keyName, StreamingFileUpload file) {
//
//    InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, keyName);
//    s3Client.initiateMultipartUpload(initRequest);
//
//    Flowable.fromPublisher(file)
//        .map(partData -> {
//          InputStream inputStream = partData.getInputStream();
//          long fileSize = file.getSize();
//          MediaType fileContentType = file.getContentType().isPresent() ?  file.getContentType().get(): null;
//
//          PutObjectRequest request = new PutObjectRequest(bucketName,
//              keyName,
//              inputStream,
//              createObjectMetadata(file)).withCannedAcl(CannedAccessControlList.PublicRead);
//          inputStream.close();
//          return tm.upload(request);
//        })
//        .subscribe(upload -> {
//          do {
//          } while(!upload.isDone());
//        });
//  }

//  @Post(value = "/2", consumes = MediaType.MULTIPART_FORM_DATA)
//  public Publisher<HttpResponse> upload(@Part UploadWithMeta file) {
//    try {
//      Publisher<Boolean> uploadPublisher = storage.storeUploadedFile(file, headers);
//      return Flowable.fromPublisher(uploadPublisher)
//          .map(success -> evaluateUpload(success, file));
//    } catch ( IOException e) {
//      log.error("Error storing uploaded file", e);
//      return Publishers.just(HttpResponse.serverError(String
//          .format("Error storing metadata for %s: %s", file.getFilename(),
//              e.getMessage())));
//    }
//  }
}
