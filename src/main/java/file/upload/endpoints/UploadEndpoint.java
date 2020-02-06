package file.upload.endpoints;

import file.upload.repository.FileServices;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;

import javax.inject.Inject;
import java.io.IOException;

@Controller( "/upload" )
public class UploadEndpoint {

  @Inject
  private FileServices file_services;

  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Post( "/" )
  public HttpResponse upload( StreamingFileUpload file ) throws IOException {
    file_services.go( file );
    return HttpResponse.ok( );
  }

//  @Consumes( MediaType.MULTIPART_FORM_DATA )
//  @Post( "/" )
//  public HttpResponse upload( Publisher<StreamingFileUpload> file ) throws IOException {
//    file.subscribe(new Subscriber<StreamingFileUpload>() {
//      protected Subscription s;
//
//      @Override
//      public void onSubscribe(Subscription s) {
//        System.out.println("onSubscribe: " + s);
//        this.s = s;
//        s.request(1);
//      }
//
//      @Override
//      public void onNext(StreamingFileUpload streamingFileUpload) {
//        System.out.println("onNext: {}" + streamingFileUpload);
//        s.request(1);
//      }
//
//      @Override
//      public void onError(Throwable t) {
//        System.out.println("onError: " + t);
//      }
//
//      @Override
//      public void onComplete() {
//        System.out.println("onComplete");
//      }
//    });
//    return HttpResponse.ok( );
//  }

}
