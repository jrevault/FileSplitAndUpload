package file.upload.spark.endpoints;

import spark.Request;
import spark.Response;
import spark.Service;

public class TestsEndpoint extends CommonEndpoint {

  private static final String CONTEXT_PATH = "/tests";

  public TestsEndpoint() {
  }

  @Override
  public void configure( Service spark , String basePath ) {
    this.spark = spark;
    String path = basePath + CONTEXT_PATH;

    spark.get( path + "/hello" , this::hello );
    spark.get( path + "/hello/:name" , this::hello );
    spark.get( path + "/ping" , this::pong );
    spark.get( path + "/conf" , this::conf );
    spark.get( path + "/crash" , this::crash );

  }

  public String hello( Request req , Response res ) {
    if ( req.params( ":name" ) != null ) {
      return send( res , "Hello " + req.params( ":name" ) );
    }
    return send( res , "Hello" );
  }

  public String pong( Request req , Response res ) {
    return send( res , "pong" );
  }

  public String conf( Request req , Response res ) {
    return send( res , "conf" );
  }

  public String crash( Request req , Response res ) {
    throw new NullPointerException( "On purpose error" );
  }

}
