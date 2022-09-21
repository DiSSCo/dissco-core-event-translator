package eu.dissco.eventdriventranslator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dissco.eventdriventranslator.client.SpecimenClient;
import eu.dissco.eventdriventranslator.domain.DigitalSpecimen;
import eu.dissco.eventdriventranslator.domain.DigitalSpecimenEvent;
import eu.dissco.eventdriventranslator.domain.EventType;
import eu.dissco.eventdriventranslator.domain.Mapping;
import eu.dissco.eventdriventranslator.repository.MappingRepository;
import io.cloudevents.CloudEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventTranslatorService {

  private final ObjectMapper mapper;
  private final KafkaService kafkaService;
  private final SpecimenClient specimenClient;
  private final MappingRepository mappingRepository;

  public JsonNode getCapabilities() {
    ObjectNode dissco = mapper.createObjectNode();
    dissco.put("name", "DiSSCo event translator service");
    dissco.put("version", "0.0.1");
    dissco.put("contact", "sam.leeflang@naturalis.nl");
    var attributes = mapper.createObjectNode();
    attributes.set("dissco", dissco);
    attributes.set("supportedeventtypes", supportedTyped());
    var data = mapper.createObjectNode();
    data.set("attributes", attributes);
    data.put("type", "capabilities4dissco");
    data.put("id", "https://dissco.eu/dissco/capabilities");
    var object = mapper.createObjectNode();
    object.set("data", data);
    var link = mapper.createObjectNode();
    link.put("self", "https://dissco.eu/dissco/capabilities");
    object.set("links", link);
    return object;
  }

  private JsonNode supportedTyped() {
    var array = mapper.createArrayNode();
    for (EventType value : EventType.values()) {
      array.add(value.getEvent());
    }
    return array;
  }

  public void postToQueue(CloudEvent cloudEvent)
      throws IOException, ExecutionException, InterruptedException {
    var data = getData(cloudEvent);
    processData(cloudEvent.getSource(), data);
  }

  private void processData(URI source, JsonNode data) throws JsonProcessingException {
    var endpointInformation = mappingRepository.retrieveEndpointInformation(source.toString());
    var mapping = endpointInformation.mapping();
    var organizationId = getProperty("organization_id", data, mapping);
    var physicalSpecimenIdType = getProperty("physical_specimen_id_type", data, mapping);
    var digitalSpecimen = new DigitalSpecimen(
        getProperty("type", data, mapping),
        getPhysicalSpecimenId(physicalSpecimenIdType, organizationId, data, mapping),
        physicalSpecimenIdType,
        getProperty("specimen_name", data, mapping),
        organizationId,
        getProperty("dataset_id", data, mapping),
        getProperty("physical_specimen_collection", data, mapping),
        endpointInformation.sourceSystemId(),
        data,
        data,
        null);
    log.info("Digital Specimen: {}", digitalSpecimen);
    var event = new DigitalSpecimenEvent(Collections.emptyList(),
        digitalSpecimen);
    log.info("Publishing digital specimen event: {}", event);
    kafkaService.sendMessage("digital-specimen", mapper.writeValueAsString(event));
  }

  private String getProperty(String fieldName, JsonNode data, Mapping mapping) {
    if (mapping.defaultMapping().containsKey(fieldName)) {
      return mapping.defaultMapping().get(fieldName);
    } else if (mapping.fieldMapping().containsKey(fieldName)) {
      var values = data.findValuesAsText(mapping.fieldMapping().get(fieldName));
      if (!values.isEmpty()) {
        return values.get(0);
      } else {
        log.warn("No values found for field: {}", fieldName);
        return null;
      }
    } else {
      log.warn("Cannot find field {}", fieldName);
      return null;
    }
  }

  private String getPhysicalSpecimenId(String physicalSpecimenIdType, String organizationId,
      JsonNode data, Mapping mapping) {
    if (physicalSpecimenIdType.equals("cetaf")) {
      return getProperty("physical_specimen_id", data, mapping);
    } else if (physicalSpecimenIdType.equals("combined")) {
      return getProperty("physical_specimen_id", data, mapping) + ":" + minifyOrganizationId(
          organizationId);
    } else {
      log.warn("Unknown physicalSpecimenIdType specified");
      return "Unknown";
    }
  }

  private String minifyOrganizationId(String organizationId) {
    if (organizationId.startsWith("https://ror.org")) {
      return organizationId.replace("https://ror.org/", "");
    } else {
      log.warn("Cannot determine organizationId: {} for combined physicalSpecimenId",
          organizationId);
      return "UnknownOrganisationUrl";
    }
  }

  private JsonNode getData(CloudEvent cloudEvent)
      throws ExecutionException, InterruptedException, IOException {
    if (EventType.OBJECT_REF.getEvent().equals(cloudEvent.getType())) {
      var requestUri = cloudEvent.getSource().toString() + '/' + cloudEvent.getSubject();
      return specimenClient.retrieveData(URI.create(requestUri));
    } else {
      return mapper.readTree(cloudEvent.getData().toBytes());
    }
  }

}
