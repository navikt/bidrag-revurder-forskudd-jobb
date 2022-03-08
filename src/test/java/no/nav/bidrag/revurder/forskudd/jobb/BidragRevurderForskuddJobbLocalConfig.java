package no.nav.bidrag.revurder.forskudd.jobb;

import static no.nav.bidrag.revurder.forskudd.jobb.BidragRevurderForskuddJobbLocal.LOCAL;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile(LOCAL)
@Configuration
@AutoConfigureWireMock(port = 8096)
public class BidragRevurderForskuddJobbLocalConfig {

  @Bean
  public Options wireMockOptions() {
    final WireMockConfiguration options = WireMockSpring.options();
    options.port(8096);
    return options;
  }
}
