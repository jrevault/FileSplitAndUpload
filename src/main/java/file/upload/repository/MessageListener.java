package file.upload.repository;

import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;

@KafkaListener( offsetReset = OffsetReset.EARLIEST )
public class MessageListener {

  private final static Logger logger = LoggerFactory.getLogger( MessageListener.class );

  private final KafkaService kafkaService;

  @Inject
  public MessageListener( KafkaService kafkaService ) {
    this.kafkaService = kafkaService;

    try {
      kafkaService.createTopic( "qq-messages" , 2 , Duration.ofSeconds( 5 ) );
    }
    catch ( Exception e ) {
      e.printStackTrace( );
    }
  }

  @Topic( "qq-messages" )
  public void receive( @KafkaKey String key , String message ) {

    System.out.println( "Got message at " + key + " : " + message );
  }

}
