package eu.dissco.eventdriventranslator.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record DigitalSpecimen(
    String type,
    String physicalSpecimenId,
    String physicalSpecimenIdType,
    String specimenName,
    String organizationId,
    String datasetId,
    String physicalSpecimenCollection,
    String sourceSystemId,
    JsonNode data,
    JsonNode originalData,
    String dwcaId) {

}
