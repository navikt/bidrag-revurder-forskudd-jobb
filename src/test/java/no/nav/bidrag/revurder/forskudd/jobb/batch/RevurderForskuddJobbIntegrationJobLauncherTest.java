package no.nav.bidrag.revurder.forskudd.jobb.batch;

import static no.nav.bidrag.revurder.forskudd.jobb.BidragRevurderForskuddJobbTest.TEST;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import no.nav.bidrag.revurder.forskudd.jobb.BidragRevurderForskuddJobbTest;
import no.nav.bidrag.revurder.forskudd.jobb.enums.InntektKategori;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

@SpringBatchTest
@SpringBootTest(classes = {BidragRevurderForskuddJobbTest.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 8096)
@ActiveProfiles(TEST)
class RevurderForskuddJobbIntegrationJobLauncherTest {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private JobRepositoryTestUtils jobRepositoryTestUtils;

  @AfterEach
  public void cleanUp() {
    jobRepositoryTestUtils.removeJobExecutions();
  }

  @Test
  void testRevurderForskuddJobb() throws Exception {

    JobParameters jobParameters = new JobParametersBuilder()
        .addString("sisteMuligeDatoForSisteVedtak", LocalDate.now().minusYears(1).toString())
        .addString("virkningsdato", LocalDate.now().plusMonths(2).withDayOfMonth(1).toString())
        .addString("inntektKategori", InntektKategori.AINNTEKT.toString())
        .addString("filLokasjon", "src/test/resources/springbatch/filer/vedtakforslag.txt")
        .toJobParameters();

    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
    assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

    String filLokasjon = jobParameters.getString("filLokasjon");
    assertThat(filLokasjon).isNotNull();

    File file = new File(filLokasjon);
    BufferedReader br = new BufferedReader(new FileReader(file));
    int antallLinjer = 0;

    while (br.readLine() != null) {
      antallLinjer++;
    }
    assertThat(antallLinjer).isEqualTo(4);
  }
}
