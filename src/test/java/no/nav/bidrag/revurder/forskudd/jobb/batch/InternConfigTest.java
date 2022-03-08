package no.nav.bidrag.revurder.forskudd.jobb.batch;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = InternConfigTest.JobConfig.class)
class InternConfigTest {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Test
  void testJob() throws Exception {
    JobExecution jobExecution = jobLauncherTestUtils.launchJob();
    assertEquals(new ExitStatus("COMPLETED").getExitCode() ,jobExecution.getExitStatus().getExitCode());
  }

  @Test
  void testStep1() {
    JobExecution jobExecution = jobLauncherTestUtils.launchStep("step1");
    assertEquals(new ExitStatus("COMPLETED").getExitCode() ,jobExecution.getExitStatus().getExitCode());
  }

  @Configuration
  @EnableBatchProcessing
  public static class JobConfig {

    @Bean
    public Step step1(StepBuilderFactory stepBuilderFactory) {
      return stepBuilderFactory.get("step1")
          .tasklet((contribution, chunkContext) -> {
            System.out.println("hello");
            return RepeatStatus.FINISHED;
          })
          .build();
    }

    @Bean
    public Step step2(StepBuilderFactory stepBuilderFactory) {
      return stepBuilderFactory.get("step2")
          .tasklet((contribution, chunkContext) -> {
            System.out.println("world");
            return RepeatStatus.FINISHED;
          })
          .build();
    }

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
      return jobBuilderFactory.get("job")
          .start(step1(stepBuilderFactory))
          .next(step2(stepBuilderFactory))
          .build();
    }

    @Bean
    public DataSource dataSource() {
      return new EmbeddedDatabaseBuilder()
          .setType(EmbeddedDatabaseType.H2)
          .addScript("/org/springframework/batch/core/schema-h2.sql")
          .build();
    }
  }
}
