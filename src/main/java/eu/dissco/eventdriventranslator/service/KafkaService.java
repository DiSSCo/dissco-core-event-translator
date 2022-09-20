package eu.dissco.eventdriventranslator.service;

import eu.dissco.eventdriventranslator.properties.KafkaProperties;
import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final KafkaProperties properties;

  public void sendMessage(String topic, String event) {
    ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(
        topic, event);
    future.addCallback(new ListenableFutureCallback<>() {

      @Override
      public void onSuccess(SendResult<String, String> result) {
        log.info("Successfully published new item to queue");
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Unable to send message: {}", event, ex);
      }
    });
  }

}
