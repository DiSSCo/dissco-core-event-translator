package eu.dissco.eventdriventranslator.domain;

import java.util.Map;

public record Mapping(
    Map<String, String> fieldMapping,
    Map<String, String> defaultMapping
) {

}
