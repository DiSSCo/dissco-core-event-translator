package eu.dissco.eventdriventranslator.repository;

import static eu.dissco.eventdriventranslator.database.jooq.Tables.NEW_MAPPING;
import static eu.dissco.eventdriventranslator.database.jooq.Tables.NEW_SOURCE_SYSTEM;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MappingRepository {

  private final ObjectMapper mapper;
  private final DSLContext context;

  public JsonNode retrieveMapping(String endpoint) {
    return context.select(NEW_MAPPING.MAPPING)
        .distinctOn(NEW_MAPPING.ID)
        .from(NEW_MAPPING)
        .join(NEW_SOURCE_SYSTEM)
        .on(NEW_SOURCE_SYSTEM.MAPPING_ID.eq(NEW_MAPPING.ID))
        .where(NEW_SOURCE_SYSTEM.ENDPOINT.eq(endpoint))
        .orderBy(NEW_MAPPING.ID, NEW_MAPPING.VERSION.desc())
        .fetchOne(this::mapToJsonNode);
  }

  private JsonNode mapToJsonNode(Record1<JSONB> jsonbMapping) {
    try {
      return mapper.readTree(jsonbMapping.value1().data());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
