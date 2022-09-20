package eu.dissco.eventdriventranslator.controller;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.eventdriventranslator.service.EventTranslatorService;
import io.cloudevents.CloudEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/dissco")
@RequiredArgsConstructor
public class EventDrivenController {

  private final EventTranslatorService service;

  @GetMapping(value = "/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonNode> getCapabilities() {
    log.info("Received request for capabilities");
    return ResponseEntity.ok(service.getCapabilities());
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping(value = "/event", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonNode> createOpenDS(Authentication authentication,
      @RequestBody CloudEvent cloudEvent)
      throws IOException, ExecutionException, InterruptedException {
    log.info("Received cloudEvent with id: {} from user: {}", cloudEvent.getId(),
        getNameFromToken(authentication));
    service.postToQueue(cloudEvent);
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  private String getNameFromToken(Authentication authentication) {
    KeycloakPrincipal<? extends KeycloakSecurityContext> principal =
        (KeycloakPrincipal<?>) authentication.getPrincipal();
    AccessToken token = principal.getKeycloakSecurityContext().getToken();
    return token.getSubject();
  }

}
