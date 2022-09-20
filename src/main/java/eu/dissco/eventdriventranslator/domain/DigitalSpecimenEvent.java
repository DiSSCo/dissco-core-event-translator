package eu.dissco.eventdriventranslator.domain;

import java.util.List;

public record DigitalSpecimenEvent(
    List<String> enrichmentList,
    DigitalSpecimen digitalSpecimen) {

}
