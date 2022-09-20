package eu.dissco.eventdriventranslator.domain;

import lombok.Getter;

@Getter
public enum EventType {

  OBJECT ("eu.dissco.translator.event.object"),
  OBJECT_REF("eu.dissco.translator.event.object-ref");

  private final String event;

  private EventType(String event) {
    this.event = event;
  }

}
