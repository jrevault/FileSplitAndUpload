package file.upload.endpoints;

import file.upload.MyProperties;
import file.upload.repository.FileServices;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.reactivex.Single;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Controller( "/upload" )
public class UploadEndpoint {

  private static Logger log = LoggerFactory.getLogger(UploadEndpoint.class);

  private MyProperties properties;
  private FileServices file_services;

  @Inject
  public UploadEndpoint( MyProperties properties , FileServices file_services ) {
    this.properties = properties;
    this.file_services = file_services;
  }

  //Try HttpResponse upload(StreamingFileUpload file, Map<String, Object> metadata) { without @Body.

  @Post( "/" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
//  public Single<HttpResponse<String>> stream( StreamingFileUpload upload_file, Map<String, Object> metadata ) {
  public Single<HttpResponse<String>> stream( StreamingFileUpload upload_file ) {

    log.info( "Name       : {}" , upload_file.getName( ) );
    log.info( "File Name  : {}" , upload_file.getFilename( ) );

    long start = System.currentTimeMillis( );

//    File final_file = properties.getDestination( "ok.tmp" ).toFile( );
    String final_file = properties.getDestination( "ok.tmp" ).getFileName( ).toString( );

    Publisher<Boolean> uploadPublisher = upload_file.transferTo( final_file );
    return Single.fromPublisher( uploadPublisher )
        .map( success -> {
          if ( !success ) {
            return HttpResponse.<String>status( HttpStatus.CONFLICT ).body( final_file );
          }
          else {
            float duration = ( System.currentTimeMillis( ) - start ) / 1000;
            float size = final_file.length( ) / 1024 / 1024;
            float throughput = size / duration;

            log.info( "Full size  : " + size + " mb" );
            log.info( "Duration   : " + duration + " s" );
            log.info( "Throughput : " + throughput + " mb/s" );
            log.info( "Final file : " + final_file );
            return HttpResponse.ok( final_file );
          }
        } );
  }

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
