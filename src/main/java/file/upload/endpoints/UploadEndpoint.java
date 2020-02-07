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

import javax.inject.Inject;
import java.io.File;

@Controller( "/upload" )
public class UploadEndpoint {

  private MyProperties properties;
  private FileServices file_services;

  @Inject
  public UploadEndpoint( MyProperties properties , FileServices file_services ) {
    this.properties = properties;
    this.file_services = file_services;
  }

  //Try HttpResponse upload(StreamingFileUpload file, Map<String, Object> metadata) { without @Body.

  @Post( "/stream" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  public Single<HttpResponse<String>> stream( StreamingFileUpload file ) {

    long start = System.currentTimeMillis( );

    File final_file = properties.getDestination( file.getFilename( ) ).toFile( );

    Publisher<Boolean> uploadPublisher = file.transferTo( final_file );
    return Single.fromPublisher( uploadPublisher )
        .map( success -> {
          if ( !success ) {
            return HttpResponse.<String>status( HttpStatus.CONFLICT ).body( final_file.getAbsolutePath( ) );
          }
          else {
            float duration = ( System.currentTimeMillis( ) - start ) / 1000;
            float size = final_file.length( ) / 1000;
            float throughput = size / duration;

            System.out.println( "Full size  : " + size + " mb" );
            System.out.println( "Duration   : " + duration + " s" );
            System.out.println( "Throughput : " + throughput + " mb/s" );
            return HttpResponse.ok( final_file.getAbsolutePath( ) );
          }
        } );
  }


}
