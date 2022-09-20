package eu.dissco.eventdriventranslator.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpecimenClient {

  private final WebClient webClient;

  public JsonNode retrieveData(URI uri) throws ExecutionException, InterruptedException {
    log.info("Requesting data from uri: {}", uri);
    return webClient.get().uri(uri).retrieve().bodyToMono(JsonNode.class).toFuture().get();
  }


}
