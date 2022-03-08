package no.nav.bidrag.revurder.forskudd.jobb;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class BidragRevurderForskuddJobb {

  public static void main(String[] args) {
    SpringApplication.run(BidragRevurderForskuddJobb.class, args);
  }
}
