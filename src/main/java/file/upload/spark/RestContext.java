package file.upload.spark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

public class RestContext {

  private static final Logger logger = LoggerFactory.getLogger( RestContext.class );

  public final Service spark;

  private final String basePath;

  public RestContext( int port , String basePath ) {
    this.basePath = basePath;
    spark = Service.ignite( ).port( port );
  }

  public void addEndpoint( EndpointBuilder endpoint ) {

    endpoint.configure( spark , basePath );
    logger.info( "REST endpoints registered for {}." , endpoint.getClass( ).getSimpleName( ) );
  }

  // Then you can even have some fun:
  public void enableCors() {

    spark.before( ( request , response ) -> {
      response.header( "Access-Control-Allow-Origin" , "*" );
      response.header( "Access-Control-Allow-Methods" , "GET, POST, PUT, DELETE, OPTIONS" );
      response.header( "Access-Control-Allow-Headers" , "Content-Type, api_key, Authorization" );
    } );

    logger.info( "CORS support enabled." );
  }
}
