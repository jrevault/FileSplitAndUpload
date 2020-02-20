package file.upload.repository;

import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.reactivex.Single;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Singleton
public class FileServices {

  Logger logger = LoggerFactory.getLogger( FileServices.class );

  public Single<Boolean> go( StreamingFileUpload file ) throws IOException {

    File tempFile = File.createTempFile( file.getFilename( ) , "temp" );
    Publisher<Boolean> uploadPublisher = file.transferTo( tempFile );
    return Single.fromPublisher( uploadPublisher )
        .map( success -> {
          if ( success ) {
            return true;
          }
          else {
            return false;
          }
        } );
    // Init kafka connection

//    Flowable.fromPublisher( file )
//        .map( partData -> {
//          InputStream inputStream = partData.getInputStream( );
//          PutObjectRequest request = new PutObjectRequest(
//              bucketName ,
//              keyName ,
//              inputStream ,
//              createObjectMetadata( file )
//          ).withCannedAcl( CannedAccessControlList.PublicRead );
//          inputStream.close( );
//          return tm.upload( request );
//        } )
//        .subscribe( upload -> {
//          do {
//          }
//          while ( !upload.isDone( ) );
//        } );
  }

  public void go( CompletedFileUpload file ) {
//    try {
//      InputStream inputStream = file.getInputStream( );
//      PutObjectRequest request = new PutObjectRequest(
//          bucketName ,
//          key ,
//          inputStream ,
//          createObjectMetadata( file )
//      ).withCannedAcl( CannedAccessControlList.PublicRead ); // <5>
//      s3Client.putObject( request );
//      inputStream.close( );
//    }
//    catch ( IOException e ) {
//      if ( logger.isErrorEnabled( ) ) {
//        logger.error( "Error occurred while uploading file " + e.getMessage( ) );
//      }
//    }
  }
}
