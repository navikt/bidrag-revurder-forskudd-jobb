package no.nav.bidrag.revurder.forskudd.jobb;

import static no.nav.bidrag.revurder.forskudd.jobb.BidragRevurderForskuddJobbTest.TEST;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile(TEST)
@Configuration
public class BidragRevurderForskuddJobbTestConfig {

  @Bean
  HttpHeaderTestRestTemplate httpHeaderTestRestTemplate() {
    TestRestTemplate testRestTemplate = new TestRestTemplate(new RestTemplateBuilder());
    return new HttpHeaderTestRestTemplate(testRestTemplate);
  }

  @Bean
  public Options wireMockOptions() {
    final WireMockConfiguration options = WireMockSpring.options();
    options.port(8096);
    return options;
  }
}
