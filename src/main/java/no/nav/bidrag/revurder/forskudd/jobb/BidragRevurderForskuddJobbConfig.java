package no.nav.bidrag.revurder.forskudd.jobb;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate;
import no.nav.bidrag.revurder.forskudd.jobb.consumer.beregn.BeregnConsumer;
import no.nav.bidrag.revurder.forskudd.jobb.consumer.grunnlag.GrunnlagConsumer;
import no.nav.bidrag.revurder.forskudd.jobb.domene.AktivtVedtak;
import no.nav.bidrag.revurder.forskudd.jobb.domene.AktivtVedtakRowMapper;
import no.nav.bidrag.revurder.forskudd.jobb.processor.AktivtVedtakItemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.H2PagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;

@Configuration
@EnableBatchProcessing
public class BidragRevurderForskuddJobbConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragRevurderForskuddJobbConfig.class);

  @Autowired
  public JobBuilderFactory jobBuilderFactory;

  @Autowired
  public StepBuilderFactory stepBuilderFactory;

  @Autowired
  public DataSource dataSource;

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
    LOGGER.info("BeregnConsumer med base url: " + beregnUrl);
    return new BeregnConsumer(restTemplate);
  }

  @Bean
  @StepScope
  public AktivtVedtakItemProcessor itemProcessor(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) {
    return new AktivtVedtakItemProcessor(grunnlagConsumer, beregnConsumer);
  }

//  @Bean
//  public AsyncItemProcessor asyncItemProcessor(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) throws Exception {
//    AsyncItemProcessor<AktivtVedtak, String> asyncItemProcessor = new AsyncItemProcessor();
//    asyncItemProcessor.setDelegate(itemProcessor(grunnlagConsumer, beregnConsumer));
//    asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());
//    asyncItemProcessor.afterPropertiesSet();
//    return asyncItemProcessor;
//  }

//  @Bean
//  @StepScope
//  public AktivtVedtakItemProcessor localPartitioningItemProcessor(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) {
//    return new AktivtVedtakItemProcessor(grunnlagConsumer, beregnConsumer);
//  }

  @Bean
//  public JdbcPagingItemReader<AktivtVedtak> itemReader() {
  public JdbcPagingItemReader<AktivtVedtak> itemReader() {

    JdbcPagingItemReader<AktivtVedtak> reader = new JdbcPagingItemReader<>();

    reader.setDataSource(this.dataSource);
    reader.setFetchSize(500);
    reader.setRowMapper(new AktivtVedtakRowMapper());

    H2PagingQueryProvider queryProvider = new H2PagingQueryProvider();
    queryProvider.setSelectClause("*");
    queryProvider.setFromClause("aktivt_vedtak");

    Map<String, Order> sortKeys = new HashMap<>(1);
    sortKeys.put("aktivt_vedtak_id", Order.ASCENDING);

    queryProvider.setSortKeys(sortKeys);

    reader.setQueryProvider(queryProvider);

    return reader;
  }

//  @Bean
//  @StepScope
//  public JdbcPagingItemReader<AktivtVedtak> localPartitioningItemReader(
//      @Value("#{stepExecutionContext['minValue']}") Long minValue,
//      @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {
//
//    System.out.println("reading " + minValue + " to " + maxValue);
//
//    JdbcPagingItemReader<AktivtVedtak> reader = new JdbcPagingItemReader<>();
//
//    reader.setDataSource(this.dataSource);
//    reader.setFetchSize(500);
//    reader.setRowMapper(new AktivtVedtakRowMapper());
//
//    H2PagingQueryProvider queryProvider = new H2PagingQueryProvider();
//    queryProvider.setSelectClause("*");
//    queryProvider.setFromClause("aktivt_vedtak");
//    queryProvider.setWhereClause("where aktivt_vedtak_id >= " + minValue + " and aktivt_vedtak_id < " + maxValue);
//
//    Map<String, Order> sortKeys = new HashMap<>(1);
//    sortKeys.put("aktivt_vedtak_id", Order.ASCENDING);
//
//    queryProvider.setSortKeys(sortKeys);
//
//    reader.setQueryProvider(queryProvider);
//
//    return reader;
//  }

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
  public FlatFileItemWriter<String> itemWriter() {
    FlatFileItemWriter<String> writer = new FlatFileItemWriter<>();
    writer.setResource(new FileSystemResource("src/test/resources/springbatch/filer/vedtakforslag.txt"));
    writer.setAppendAllowed(false);
    writer.setLineAggregator(new PassThroughLineAggregator<>());
    return writer;
  }

//  @Bean
//  public AsyncItemWriter asyncItemWriter() throws Exception {
//    AsyncItemWriter<String> asyncItemWriter = new AsyncItemWriter<>();
//    asyncItemWriter.setDelegate(itemWriter());
//    asyncItemWriter.afterPropertiesSet();
//    return asyncItemWriter;
//  }

//  @Bean
//  @StepScope
//  public FlatFileItemWriter<String> localPartitioningItemWriter() {
//    FlatFileItemWriter<String> writer = new FlatFileItemWriter<>();
//    writer.setResource(new FileSystemResource("src/test/resources/springbatch/filer/vedtakforslag.txt"));
//    writer.setAppendAllowed(false);
//    writer.setLineAggregator(new PassThroughLineAggregator<>());
//    return writer;
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
  public Step sequentialProcessingStep(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) {
//  public Step sequentialProcessingStep(StepBuilderFactory stepBuilderFactory, GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) {
    return stepBuilderFactory.get("step")
        .<AktivtVedtak, String>chunk(500)
        .reader(itemReader())
        .processor(itemProcessor(grunnlagConsumer, beregnConsumer))
        .writer(itemWriter())
        .build();
  }

//  @Bean
//  public Step multiThreadedProcessingStep(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) throws IOException {
//    return stepBuilderFactory.get("step")
//        .<AktivtVedtak, String>chunk(500)
//        .reader(itemReader())
//        .processor(itemProcessor(grunnlagConsumer, beregnConsumer))
//        .writer(itemWriter())
//        .taskExecutor(new SimpleAsyncTaskExecutor())
//        .build();
//  }

//  @Bean
//  public Step asyncProcessingStep(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) throws Exception {
//    return stepBuilderFactory.get("step")
//        .<AktivtVedtak, String>chunk(500)
//        .reader(itemReader())
//        .processor(asyncItemProcessor(grunnlagConsumer, beregnConsumer))
//        .writer(asyncItemWriter())
//        .build();
//  }

//  @Bean
//  public Step localPartitioningProcessingStep(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) {
//    return stepBuilderFactory.get("step")
//        .partitioner(slaveStep(grunnlagConsumer, beregnConsumer).getName(), partitioner())
//        .step(slaveStep(grunnlagConsumer, beregnConsumer))
//        .gridSize(4)
//        .taskExecutor(new SimpleAsyncTaskExecutor())
//        .build();
//  }

//  @Bean
//  public ColumnRangePartitioner partitioner() {
//    ColumnRangePartitioner columnRangePartitioner = new ColumnRangePartitioner();
//    columnRangePartitioner.setColumn("aktivt_vedtak_id");
//    columnRangePartitioner.setDataSource(dataSource);
//    columnRangePartitioner.setTable("aktivt_vedtak");
//    return columnRangePartitioner;
//  }

//  @Bean
//  public Step slaveStep(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) {
//    return stepBuilderFactory.get("slaveStep")
//        .<AktivtVedtak, String>chunk(500)
//        .reader(localPartitioningItemReader(null, null))
//        .processor(localPartitioningItemProcessor(grunnlagConsumer, beregnConsumer))
//        .writer(localPartitioningItemWriter())
//        .build();
//  }

  @Bean
  @Primary
  public Job sequentialProcessingJob(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) throws Exception {
//  public Job sequentialProcessingJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, GrunnlagConsumer grunnlagConsumer,
//      BeregnConsumer beregnConsumer) {
    return jobBuilderFactory.get("job")
        .start(sequentialProcessingStep(grunnlagConsumer, beregnConsumer))
//        .start(sequentialProcessingStep(stepBuilderFactory, grunnlagConsumer, beregnConsumer))
        .build();
  }

//  @Bean
//  public Job multiThreadedProcessingJob(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) throws Exception {
//    return jobBuilderFactory.get("job")
//        .start(multiThreadedProcessingStep(grunnlagConsumer, beregnConsumer))
//        .build();
//  }

//  @Bean
//  public Job asyncProcessingJob(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) throws Exception {
//    return jobBuilderFactory.get("job")
//        .start(asyncProcessingStep(grunnlagConsumer, beregnConsumer))
//        .build();
//  }

//  @Bean
//  public Job localPartitioningProcessingJob(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) throws Exception {
//    return jobBuilderFactory.get("job")
//        .start(localPartitioningProcessingStep(grunnlagConsumer, beregnConsumer))
//        .build();
//  }

//  @Bean
//  public DataSource dataSource() {
//    return new EmbeddedDatabaseBuilder()
//        .setType(EmbeddedDatabaseType.H2)
//        .addScript("/org/springframework/batch/core/schema-h2.sql")
//        .addScript("schema.sql")
//        .addScript("data.sql")
//        .build();
//  }
}
