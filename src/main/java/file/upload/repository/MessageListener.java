package file.upload.repository;

import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaListener( offsetReset = OffsetReset.EARLIEST )
public class MessageListener {

  @Topic( "qq-messages" )
  public void receive( @KafkaKey String key , String message ) {
    System.out.println( "Got message at " + key + " : " + message );
  }

}
