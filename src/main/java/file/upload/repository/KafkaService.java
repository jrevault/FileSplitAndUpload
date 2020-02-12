package file.upload.repository;

import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class KafkaService {

  private final static Logger logger = LoggerFactory.getLogger( KafkaService.class );

  private static final int CHECK_TOPIC_RETRY_COUNT = 60;
  private static final int CHECK_TOPIC_SLEEP_MS = 5000;
  private static final Short REPLICATION_FACTOR = 1;

  private final AdminClient adminClient;

  @Inject
  public KafkaService(AdminClient adminClient) {
    this.adminClient = adminClient;
  }

  public void createTopic(String topicName, int partitionCount, Duration retentionTime) throws Exception {
    if (!topicExist(topicName)) {
        logger.debug("Create topic '{}' operation started", topicName);

      if (createTopicWithVerification(topicName, partitionCount)) {
        logger.info("Topic '{}' successfully created with {} partitions", topicName, partitionCount);
        if (retentionTime != null) {
          configureTopicRetention(adminClient, topicName, retentionTime);
          logger.info("Topic '{}' successfully configured with retention time: {}",
                  topicName, retentionTime);
        }
      } else {
        throw new Exception("Topic creation failed: '" + topicName + "' can not to be created");
      }
    } else {
      logger.warn("Impossible to create topic: '" + topicName + "' already exists");
    }
  }

  private boolean createTopicWithVerification(String topicName, int partitionCount) throws Exception {
    try {
      boolean topicCreated = false;
      NewTopic newTopic = new NewTopic(topicName, partitionCount, REPLICATION_FACTOR);
      if (createNewTopicWithRetry(newTopic)) {
        topicCreated = checkTopicCreated(topicName);
      }
      return topicCreated;
    } catch (Exception e) {
      throw new Exception("Error occurred while creating topic " + topicName, e);
    }
  }

  private boolean createNewTopicWithRetry(NewTopic newTopic) throws InterruptedException {
    boolean operationFinished = false;
    for (int i = 0; (i < CHECK_TOPIC_RETRY_COUNT && !operationFinished); i++) {
      try {
        CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
        result.all().get(); // wait for creation command to finish
        operationFinished = true;
      } catch (Exception e) {
        logger.debug("Error while creating new topic '{}' - Retry operation ({}/{}) in {} ms - {}",
                newTopic.name(), i+1, CHECK_TOPIC_RETRY_COUNT, CHECK_TOPIC_SLEEP_MS, e.getMessage());
        Thread.sleep(CHECK_TOPIC_SLEEP_MS);
      }
    }
    return operationFinished;
  }

  private static synchronized void configureTopicRetention(AdminClient adminClient, String topicName, Duration retentionTime) {
    ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
    ConfigEntry retentionConfig = new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, Long.toString(retentionTime.toMillis()));
    Map<ConfigResource, Config> configs = new HashMap<>();
    configs.put(resource, new Config(Collections.singleton(retentionConfig)));

    adminClient.alterConfigs(configs).all().whenComplete((v, t) -> {
      if (t == null) {
          logger.debug("Topic '{}' configured using admin client / Retention time: {} ms",
                  topicName, retentionTime.toMillis());
      } else {
        throw new RuntimeException("Could not configure retention for topic " + topicName, t);
      }
    });
  }

  private boolean checkTopicCreated(String topicName) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("Verify topic '{}' creation", topicName);
    }
    boolean isCreated = false;
    for (int i = 0; (i < CHECK_TOPIC_RETRY_COUNT && !isCreated); i++) {
      try {
        isCreated = topicExist(topicName);
        if (!isCreated) {
          if (logger.isDebugEnabled()) {
            logger.debug("Topic '{}' is not created yet. Need to check again if creation completed.", topicName);
          }
          Thread.sleep(CHECK_TOPIC_SLEEP_MS);
        }
      } catch (Exception e) {
        throw new Exception("Exception occurred while checking for topic creation", e);
      }
    }
    return isCreated;
  }


  private boolean topicExist(String topicName) {
    boolean topicExists = false;
    try {
      topicExists = adminClient.listTopics().names().thenApply(names -> names.contains(topicName)).get();
    } catch (InterruptedException | ExecutionException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Error while checking if topic '{}' exists - {}", topicName, e);
      }
    }
    if (logger.isDebugEnabled()) {
      if (topicExists) {
        logger.debug("Check if topic '{}' exists: topic exists", topicName);
      } else {
        logger.debug("Check if topic '{}' exists: topic does not exist", topicName);
      }
    }
    return topicExists;
  }
}
