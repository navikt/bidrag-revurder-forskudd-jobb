package no.nav.bidrag.revurder.forskudd.jobb;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate;
import no.nav.bidrag.revurder.forskudd.jobb.beregn.consumer.BeregnConsumer;
import no.nav.bidrag.revurder.forskudd.jobb.domene.AktivtVedtak;
import no.nav.bidrag.revurder.forskudd.jobb.domene.AktivtVedtakRowMapper;
import no.nav.bidrag.revurder.forskudd.jobb.domene.JobbParameter;
import no.nav.bidrag.revurder.forskudd.jobb.enums.InntektKategori;
import no.nav.bidrag.revurder.forskudd.jobb.grunnlag.consumer.GrunnlagConsumer;
import no.nav.bidrag.revurder.forskudd.jobb.processor.AktivtVedtakItemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.H2PagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;

@Configuration
@AutoConfigureWireMock(port = 8096)
public class BidragRevurderForskuddJobbConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragRevurderForskuddJobbConfig.class);

  @Autowired
  public JobBuilderFactory jobBuilderFactory;

  @Autowired
  public StepBuilderFactory stepBuilderFactory;

  @Autowired
  public DataSource dataSource;

//  @Bean
//  public JdbcCursorItemReader<AktivtVedtak> cursorItemReader() {
//    JdbcCursorItemReader<AktivtVedtak> reader = new JdbcCursorItemReader<>();
//
//    reader.setSql("select * from aktivt_vedtak order by aktivt_vedtak_id");
//    reader.setDataSource(this.dataSource);
//    reader.setRowMapper(new AktivtVedtakRowMapper());
//
//    return reader;
//  }

  @Bean
  public JobbParameter jobbParameter() {
    return new JobbParameter(
        LocalDate.now().minusYears(1),
        LocalDate.now().plusMonths(2).withDayOfMonth(1),
        InntektKategori.AINNTEKT
    );
  }

  @Bean
  public JdbcPagingItemReader<AktivtVedtak> itemReader() {
    JdbcPagingItemReader<AktivtVedtak> reader = new JdbcPagingItemReader<>();

    reader.setDataSource(this.dataSource);
    reader.setFetchSize(10);
    reader.setRowMapper(new AktivtVedtakRowMapper());

    H2PagingQueryProvider queryProvider = new H2PagingQueryProvider();
    queryProvider.setSelectClause("*");
    queryProvider.setFromClause("aktivt_vedtak");

    Map<String, Order> sortKeys = new HashMap<>(2);
    sortKeys.put("aktivt_vedtak_id", Order.ASCENDING);

    queryProvider.setSortKeys(sortKeys);

    reader.setQueryProvider(queryProvider);

    return reader;
  }
//
//  @Bean
//  public ItemWriter<String> itemWriter() {
//    return items -> {
//      for (String item : items) {
//        System.out.println(item);
//      }
//    };
//  }

//  @Bean
//  public JsonFileItemWriter<AktivtVedtak> itemWriter() throws IOException {
//    JsonFileItemWriterBuilder<AktivtVedtak> builder = new JsonFileItemWriterBuilder<>();
//    JacksonJsonObjectMarshaller<AktivtVedtak> marshaller = new JacksonJsonObjectMarshaller<>();
//    String output = File.createTempFile("aktivtVedtakOutput", ".json").getAbsolutePath();
//    return builder
//        .name("aktivtVedtakItemWriter")
//        .jsonObjectMarshaller(marshaller)
//        .resource(new FileSystemResource(output))
//        .build();
//  }

  @Bean
  public FlatFileItemWriter<String> itemWriter() {
    FlatFileItemWriter<String> writer = new FlatFileItemWriter<>();
    writer.setResource(new FileSystemResource("src/main/resources/filer/vedtakforslag.txt"));
    writer.setAppendAllowed(false);
    writer.setLineAggregator(new PassThroughLineAggregator<>());
    return writer;
  }

  @Bean
  public CorrelationIdFilter correlationIdFilter() {
    return new CorrelationIdFilter();
  }

  @Bean
  @Scope("prototype")
  public HttpHeaderRestTemplate restTemplate() {
    var httpHeaderRestTemplate = new HttpHeaderRestTemplate();
    httpHeaderRestTemplate.addHeaderGenerator(CorrelationIdFilter.CORRELATION_ID_HEADER, CorrelationIdFilter::fetchCorrelationIdForThread);
    return httpHeaderRestTemplate;
  }

  @Bean
  public GrunnlagConsumer grunnlagConsumer(@Value("${GRUNNLAG_URL}") String grunnlagUrl, HttpHeaderRestTemplate restTemplate) {
    restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(grunnlagUrl));
    LOGGER.info("GrunnlagConsumer med base url: " + grunnlagUrl);
    return new GrunnlagConsumer(restTemplate);
  }

  @Bean
  public BeregnConsumer beregnConsumer(@Value("${BEREGN_URL}") String beregnUrl, HttpHeaderRestTemplate restTemplate) {
    restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(beregnUrl));
    LOGGER.info("GrunnlagConsumer med base url: " + beregnUrl);
    return new BeregnConsumer(restTemplate);
  }

  @Bean
  public AktivtVedtakItemProcessor itemProcessor(JobbParameter jobbParameter, GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) {
    return new AktivtVedtakItemProcessor(jobbParameter, grunnlagConsumer, beregnConsumer);
  }

//  @Bean
//  public ValidatingItemProcessor<AktivtVedtak> itemProcessor() {
//    return new ValidatingItemProcessor<>(new AktivtVedtakValidator());
//  }

  @Bean
  public Step step1(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) throws IOException {
    return stepBuilderFactory.get("step1")
        .<AktivtVedtak, String>chunk(10)
        .reader(itemReader())
        .processor(itemProcessor(jobbParameter(), grunnlagConsumer, beregnConsumer))
        .writer(itemWriter())
        .build();
  }

  @Bean
  public Job job(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) throws IOException {
    return jobBuilderFactory.get("job")
        .start(step1(grunnlagConsumer, beregnConsumer))
        .build();
  }

  @Bean
  public Options wireMockOptions() {
    final WireMockConfiguration options = WireMockSpring.options();
    options.port(8096);
    return options;
  }
}
