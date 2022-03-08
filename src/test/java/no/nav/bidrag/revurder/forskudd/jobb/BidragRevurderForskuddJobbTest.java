package no.nav.bidrag.revurder.forskudd.jobb;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class BidragRevurderForskuddJobbTest {

  public static final String TEST = "test";

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(BidragRevurderForskuddJobbTest.class);
    app.setAdditionalProfiles(TEST);
    app.run(args);
  }
}
