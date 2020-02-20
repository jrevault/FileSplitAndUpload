package file.upload.repository;

import io.micronaut.configuration.kafka.annotation.KafkaKey;

//@KafkaClient
public interface MessageProducer {

  //  @Topic( "qq-messages" )
  void send( @KafkaKey String key , String message );

}
