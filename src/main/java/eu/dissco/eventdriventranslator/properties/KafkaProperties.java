package eu.dissco.eventdriventranslator.properties;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("kafka")
public class KafkaProperties {

  @NotBlank
  private String host;

  @NotBlank
  private String topic;

}
