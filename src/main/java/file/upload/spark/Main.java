package file.upload.spark;

import file.upload.spark.endpoints.TestsEndpoint;
import file.upload.spark.endpoints.UploadEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {


  private static final Logger LOG = LoggerFactory.getLogger( Main.class );


  public static void main( String[] args ) {


    RestContext context = new RestContext( 8080 , "/" );
    context.addEndpoint( new TestsEndpoint( ) );
    context.addEndpoint( new UploadEndpoint( ) );

    context.enableCors( );

  }

}
