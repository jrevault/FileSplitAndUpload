package file.upload.spark.endpoints;

import com.google.gson.Gson;
import file.upload.spark.EndpointBuilder;
import file.upload.spark.StandardResponse;
import file.upload.spark.StatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;
import spark.Service;

public abstract class CommonEndpoint implements EndpointBuilder {

  protected static final Logger LOG = LoggerFactory.getLogger( CommonEndpoint.class );

  Service spark;

  String send( Response res , Object answer ) {
    res.type( "application/json" );
    return new Gson( ).toJson(
        new StandardResponse(
            StatusResponse.OK ,
            new Gson( ).toJsonTree( answer )
        ) );
  }

}
