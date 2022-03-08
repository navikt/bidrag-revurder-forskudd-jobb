package no.nav.bidrag.revurder.forskudd.jobb.batch;

import java.time.LocalDate;
import no.nav.bidrag.revurder.forskudd.jobb.BidragRevurderForskuddJobbTest;
import no.nav.bidrag.revurder.forskudd.jobb.enums.InntektKategori;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

@SpringBatchTest
@SpringBootTest(classes = {BidragRevurderForskuddJobbTest.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
//@ExtendWith(SpringExtension.class)
@AutoConfigureWireMock(port = 8096)
//@ContextConfiguration(classes = {BidragRevurderForskuddJobbConfig.class, BidragRevurderForskuddJobbTestConfig.class})
class RevurderForskuddJobbIntegrationTest {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Test
  void testRevurderForskuddJobb() throws Exception {

    JobParameters jobParameters = new JobParametersBuilder()
        .addString("sisteMuligeDatoForSisteVedtak", LocalDate.now().minusYears(1).toString())
        .addString("virkningsdato", LocalDate.now().plusMonths(2).withDayOfMonth(1).toString())
        .addString("inntektskategori", InntektKategori.AINNTEKT.toString())
        .toJobParameters();

    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
  }

//  @Test
//  public void testProcessor() throws Exception {
//
//    AktivtVedtak aktivtVedtak = new AktivtVedtak(1, 1, "1", "1", "1", LocalDate.now(), LocalDate.now(), "X", BigDecimal.ZERO, "NOK", "X", "ENSLIG", 0,
//        "ALENE", LocalDate.now(), false, "TEST", LocalDateTime.now());
//    assertNotNull(itemProcessor.process(aktivtVedtak));
//  }

}
