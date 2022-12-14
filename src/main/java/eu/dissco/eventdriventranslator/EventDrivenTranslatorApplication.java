package eu.dissco.eventdriventranslator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableKafka
public class EventDrivenTranslatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventDrivenTranslatorApplication.class, args);
	}

}
