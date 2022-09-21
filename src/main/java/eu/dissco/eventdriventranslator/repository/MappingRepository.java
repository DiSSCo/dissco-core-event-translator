package eu.dissco.eventdriventranslator.repository;

import static eu.dissco.eventdriventranslator.database.jooq.Tables.NEW_MAPPING;
import static eu.dissco.eventdriventranslator.database.jooq.Tables.NEW_SOURCE_SYSTEM;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.eventdriventranslator.domain.EndpointInformation;
import eu.dissco.eventdriventranslator.domain.Mapping;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record2;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MappingRepository {

  private final ObjectMapper mapper;
  private final DSLContext context;

  public EndpointInformation retrieveEndpointInformation(String endpoint) {
    return context.select(NEW_MAPPING.MAPPING, NEW_SOURCE_SYSTEM.ID)
        .distinctOn(NEW_MAPPING.ID)
        .from(NEW_MAPPING)
        .join(NEW_SOURCE_SYSTEM)
        .on(NEW_SOURCE_SYSTEM.MAPPING_ID.eq(NEW_MAPPING.ID))
        .where(NEW_SOURCE_SYSTEM.ENDPOINT.eq(endpoint))
        .orderBy(NEW_MAPPING.ID, NEW_MAPPING.VERSION.desc())
        .fetchOne(this::mapEndpointInformation);
  }

  private EndpointInformation mapEndpointInformation(Record2<JSONB, String> dbRecord) {
    try {
      var mapping = mapMapping(dbRecord.value1());
      return new EndpointInformation(dbRecord.value2(), mapping);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public Mapping mapMapping(JSONB mapping) throws JsonProcessingException {
    var mappingJson = mapper.readTree(mapping.data());
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
