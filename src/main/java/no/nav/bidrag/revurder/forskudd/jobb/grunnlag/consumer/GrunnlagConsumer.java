package no.nav.bidrag.revurder.forskudd.jobb.grunnlag.consumer;

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.revurder.forskudd.jobb.grunnlag.api.HentKomplettGrunnlagspakkeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

public class GrunnlagConsumer {
  private static final Logger LOGGER = LoggerFactory.getLogger(GrunnlagConsumer.class);
  private static final String ENDPOINT_HENT_GRUNNLAGSPAKKE = "/grunnlagspakke/";
  private static final ParameterizedTypeReference<HentKomplettGrunnlagspakkeResponse> RESPONSE_HENT_GRUNNLAGSPAKKE = new ParameterizedTypeReference<>() {
  };

  private final HttpHeaderRestTemplate restTemplate;

  public GrunnlagConsumer(HttpHeaderRestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public HttpResponse<HentKomplettGrunnlagspakkeResponse> hentGrunnlagspakke(Integer grunnlagspakkeId) {
    var uri = UriComponentsBuilder.fromPath(ENDPOINT_HENT_GRUNNLAGSPAKKE + grunnlagspakkeId).toUriString();
    LOGGER.info("Hent grunnlagspakke uri: {}", uri);

    try {
      var response = restTemplate.exchange(uri, HttpMethod.GET, null, RESPONSE_HENT_GRUNNLAGSPAKKE);
      return HttpResponse.from(response.getStatusCode(), response.getBody());
    } catch (HttpClientErrorException e) {
      var melding =
          "Feil ved kall til bidrag-grunnlag hent grunnlagspakke API: " + e.getMessage() + ". Response body: " + e.getResponseBodyAsString();
      LOGGER.error(melding);
//      throw new BidragGrunnlagConsumerException(melding, e.getStatusCode());
      throw new RuntimeException(melding);
    }
  }
}
