package no.nav.bidrag.revurder.forskudd.jobb.beregn.consumer;

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.revurder.forskudd.jobb.beregn.api.BeregnForskuddGrunnlag;
import no.nav.bidrag.revurder.forskudd.jobb.beregn.api.BeregnetForskuddResultat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

public class BeregnConsumer {
  private static final Logger LOGGER = LoggerFactory.getLogger(BeregnConsumer.class);
  private static final String ENDPOINT_BEREGN_FORSKUDD = "/forskudd";
  private static final ParameterizedTypeReference<BeregnetForskuddResultat> RESPONSE_BEREGN_FORSKUDD = new ParameterizedTypeReference<>() {
  };

  private final HttpHeaderRestTemplate restTemplate;

  public BeregnConsumer(HttpHeaderRestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public HttpResponse<BeregnetForskuddResultat> beregnForskudd(BeregnForskuddGrunnlag beregnForskuddGrunnlag) {
    var uri = UriComponentsBuilder.fromPath(ENDPOINT_BEREGN_FORSKUDD).toUriString();
    var request = createRequestEntity(beregnForskuddGrunnlag);
    LOGGER.info("Beregn forskudd uri: {}", uri);

    try {
      var response = restTemplate.exchange(uri, HttpMethod.POST, request, RESPONSE_BEREGN_FORSKUDD);
      return HttpResponse.from(response.getStatusCode(), response.getBody());
    } catch (HttpClientErrorException e) {
      var melding =
          "Feil ved kall til bidrag-beregn-forskudd-rest API: " + e.getMessage() + ". Response body: " + e.getResponseBodyAsString();
      LOGGER.error(melding);
//      throw new BidragBeregnConsumerException(melding, e.getStatusCode());
      throw new RuntimeException(melding);
    }
  }

  private <T> HttpEntity<T> createRequestEntity(T body) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity(body, headers);
  }
}
