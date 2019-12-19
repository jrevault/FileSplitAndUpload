package file.upload.endpoints;

import file.upload.repository.FileServices;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;

import javax.inject.Inject;

@Controller( "/upload" )
public class UploadEndpoint {

  @Inject
  private FileServices file_services;

  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Post( "/" )
  public HttpResponse upload( StreamingFileUpload file ) {
    file_services.go( file );
    return HttpResponse.ok( );
  }

}
