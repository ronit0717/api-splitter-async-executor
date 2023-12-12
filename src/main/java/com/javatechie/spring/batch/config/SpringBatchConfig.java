package com.javatechie.spring.batch.config;

import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.enumeration.BatchRequestItemExecutionStatus;
import com.javatechie.spring.batch.repository.BatchRequestItemRepository;
import com.javatechie.spring.batch.repository.BatchRequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private BatchRequestRepository batchRequestRepository;

    @Autowired
    private BatchRequestItemRepository batchRequestItemRepository;

    @Autowired
    private JobRepository jobRepository;

    @Bean
    public BatchRequestItemProcessor itemProcessor() {

        return new BatchRequestItemProcessor();
    }

    @Bean
    public BatchRequestStepListener stepListener() {

        return new BatchRequestStepListener(batchRequestRepository, batchRequestItemRepository);
    }

    @Bean
    public BatchRequestJobListener jobListener() {

        return new BatchRequestJobListener();
    }

    @Bean
    public BatchRequestItemWriter itemWriter() {
        return new BatchRequestItemWriter(batchRequestItemRepository);
    }

    @Bean
    @StepScope
    public RepositoryItemReader<BatchRequestEntityItem> itemReader(@Value("#{jobParameters[batch_request_id]}") Long batchRequestId) {
        RepositoryItemReader<BatchRequestEntityItem> reader = new RepositoryItemReader<>();
        reader.setRepository(batchRequestItemRepository);
        reader.setMethodName("findAllByBatchRequestId");
        reader.setArguments(Collections.singletonList(batchRequestId));
        reader.setPageSize(10000);
        Map<String, Sort.Direction> map = new HashMap<>();
        map.put("id", Sort.Direction.ASC);
        reader.setSort(map);
        return reader;
    }

    @Bean
    public Step processBatchStep() {

        return stepBuilderFactory.get("bulkProcessingBatchStep").<BatchRequestEntityItem, BatchRequestEntityItem>chunk(2)
              .reader(itemReader(null))
              .processor(itemProcessor())
              .writer(itemWriter())
              .taskExecutor(taskExecutor())
              .listener(stepListener()) //Updates the BatchExecutionStatus of BatchRequestEntity
              .build();
    }

    @Bean(name="bulkProcessingJob")
    public Job runBulkProcessingJob() {
        return jobBuilderFactory.get("processBulkProcessingBatch")
              //.listener(jobListener()) //Ensures only one job is getting executed at a time in this JVM
              .flow(processBatchStep()).end().build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(2);
        return asyncTaskExecutor;
    }

    @Bean(name = "batchJobLauncher")
    public JobLauncher simpleJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

}
