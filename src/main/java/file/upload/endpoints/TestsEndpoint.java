package file.upload.endpoints;

import file.upload.repository.MessageProducer;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Controller( "/tests" )
public class TestsEndpoint {

  private static Logger log = LoggerFactory.getLogger(TestsEndpoint.class);

  @Inject
  private MessageProducer message_producer;

  @Get( value = "/ping", produces = MediaType.TEXT_PLAIN )
  public String ping() {
    return "pong";
  }

  @Get( value = "/kafka", produces = MediaType.TEXT_PLAIN )
  public String kafka( String message ) {
    message_producer.send( String.valueOf( System.currentTimeMillis( ) ) , message );
    return "pong";
  }

}
