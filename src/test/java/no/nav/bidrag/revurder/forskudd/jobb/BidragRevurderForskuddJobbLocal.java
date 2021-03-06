package no.nav.bidrag.revurder.forskudd.jobb;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class BidragRevurderForskuddJobbLocal {

  public static final String LOCAL = "local";

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(BidragRevurderForskuddJobbLocal.class);
    app.setAdditionalProfiles(LOCAL);
    app.run(args);
  }
}
