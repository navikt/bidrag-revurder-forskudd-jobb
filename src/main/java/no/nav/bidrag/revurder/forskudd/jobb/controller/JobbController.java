package no.nav.bidrag.revurder.forskudd.jobb.controller;

import java.time.LocalDate;
import no.nav.bidrag.revurder.forskudd.jobb.enums.InntektKategori;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobbController {

  private final JobLauncher jobLauncher;
  private final Job revurderForskuddJobb;

  public JobbController(JobLauncher jobLauncher, Job revurderForskuddJobb) {
    this.jobLauncher = jobLauncher;
    this.revurderForskuddJobb = revurderForskuddJobb;
  }

  @RequestMapping("/startjobb")
  public String startJobb() throws Exception {

    JobParameters jobParameters = new JobParametersBuilder()
        .addString("sisteMuligeDatoForSisteVedtak", LocalDate.now().minusYears(1).toString())
        .addString("virkningsdato", LocalDate.now().plusMonths(2).withDayOfMonth(1).toString())
        .addString("inntektKategori", InntektKategori.AINNTEKT.toString())
        .addString("filLokasjon", "src/test/resources/springbatch/filer/vedtakforslag.txt")
        .toJobParameters();

    var jobExecution = jobLauncher.run(revurderForskuddJobb, jobParameters);
    return "Revurder forskudd batchjobb ble utført med følgende exit status: " + jobExecution.getExitStatus();
  }
}
