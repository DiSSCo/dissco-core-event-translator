package eu.dissco.eventdriventranslator.service;

import eu.dissco.eventdriventranslator.domain.Mapping;
import eu.dissco.eventdriventranslator.repository.MappingRepository;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MappingService {

  private final MappingRepository repository;

  public Mapping retrieveMapping(String endpoint) {
    var mappingJson = repository.retrieveMapping(endpoint);
    var fieldMapping = new HashMap<String, String>();
    var defaultMapping = new HashMap<String, String>();
    var mappingObject = mappingJson.get("mapping");
    mappingObject.iterator().forEachRemaining(node -> node.fields()
        .forEachRemaining(field -> fieldMapping.put(field.getKey(), field.getValue().asText())));
    var defaultObject = mappingJson.get("defaults");
    defaultObject.iterator().forEachRemaining(node -> node.fields()
        .forEachRemaining(field -> defaultMapping.put(field.getKey(), field.getValue().asText())));
    return new Mapping(fieldMapping, defaultMapping);
  }

}
