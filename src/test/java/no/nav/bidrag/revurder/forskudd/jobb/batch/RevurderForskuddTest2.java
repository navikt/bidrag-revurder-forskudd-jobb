package no.nav.bidrag.revurder.forskudd.jobb.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import no.nav.bidrag.revurder.forskudd.jobb.BidragRevurderForskuddJobbConfig;
import no.nav.bidrag.revurder.forskudd.jobb.enums.InntektKategori;
import org.junit.After;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

//@RunWith(SpringRunner.class)
@SpringBatchTest
@EnableAutoConfiguration
@ContextConfiguration(classes = { BidragRevurderForskuddJobbConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Disabled
public class RevurderForskuddTest2 {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private JobRepositoryTestUtils jobRepositoryTestUtils;

  @After
  public void cleanUp() {
    jobRepositoryTestUtils.removeJobExecutions();
  }

  private JobParameters defaultJobParameters() {
    JobParametersBuilder paramsBuilder = new JobParametersBuilder();
    paramsBuilder.addString("sisteMuligeDatoForSisteVedtak", LocalDate.now().minusYears(1).toString());
    paramsBuilder.addString("virkningsdato", LocalDate.now().plusMonths(2).withDayOfMonth(1).toString());
    paramsBuilder.addString("inntektskategori", InntektKategori.AINNTEKT.toString());
    return paramsBuilder.toJobParameters();
  }

  @Test
  public void test() throws Exception {
    // given
//    FileSystemResource expectedResult = new FileSystemResource(EXPECTED_OUTPUT);
//    FileSystemResource actualResult = new FileSystemResource(TEST_OUTPUT);

    // when
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
    JobInstance actualJobInstance = jobExecution.getJobInstance();
    ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

    // then
    assertThat(actualJobInstance.getJobName()).isEqualTo("transformBooksRecords");
    assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
//    AssertFile.assertFileEquals(expectedResult, actualResult);
  }
}