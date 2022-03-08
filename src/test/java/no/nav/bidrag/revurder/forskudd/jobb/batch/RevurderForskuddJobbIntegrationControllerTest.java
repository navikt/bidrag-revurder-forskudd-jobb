package no.nav.bidrag.revurder.forskudd.jobb.batch;

import static no.nav.bidrag.revurder.forskudd.jobb.BidragRevurderForskuddJobbTest.TEST;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.bidrag.revurder.forskudd.jobb.BidragRevurderForskuddJobbTest;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

@SpringBatchTest
@SpringBootTest(classes = {BidragRevurderForskuddJobbTest.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 8096)
@ActiveProfiles(TEST)
class RevurderForskuddJobbIntegrationControllerTest {

  @Autowired
  private HttpHeaderTestRestTemplate httpHeaderTestRestTemplate;

  @LocalServerPort
  private int port;

  @Test
  void testRevurderForskuddJobb() throws Exception {

    String url = "http://localhost:" + port + "/startjobb";
    var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, String.class);
    var response = responseEntity.getBody();
    assertThat(response).isEqualTo("Revurder forskudd batchjobb ble utført med følgende exit status: " + ExitStatus.COMPLETED);

    String filLokasjon = "src/test/resources/springbatch/filer/vedtakforslag.txt";

    File file = new File(filLokasjon);
    BufferedReader br = new BufferedReader(new FileReader(file));
    int antallLinjer = 0;

    while (br.readLine() != null) {
      antallLinjer++;
    }
    assertThat(antallLinjer).isEqualTo(4);
  }
}
