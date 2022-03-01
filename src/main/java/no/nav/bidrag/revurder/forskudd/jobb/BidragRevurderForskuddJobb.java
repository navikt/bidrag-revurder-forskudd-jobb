package no.nav.bidrag.revurder.forskudd.jobb;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableBatchProcessing
public class BidragRevurderForskuddJobb {

  public static void main(String[] args) {
//    SpringApplication.run(BidragRevurderForskuddJobb.class, args);

    SpringApplication app = new SpringApplication(BidragRevurderForskuddJobb.class);
    ConfigurableApplicationContext context = app.run(args);
    SpringApplication.exit(context);
//    context.getBean(GrunnlagConsumerApiStub.class).settOppGrunnlagStub();
//    context.getBean(BeregningApiStub.class).settOppBeregningStub();
  }
}
