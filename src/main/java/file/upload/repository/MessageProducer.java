package file.upload.repository;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaClient
public interface MessageProducer {

  @Topic( "qq-messages" )
  void send( @KafkaKey String key , String message );

}
