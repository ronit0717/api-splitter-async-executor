package com.javatechie.spring.batch.config;

import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.entity.Customer;
import com.javatechie.spring.batch.enumeration.BatchRequestItemExecutionStatus;
import com.javatechie.spring.batch.repository.BatchRequestItemRepository;
import com.javatechie.spring.batch.repository.BatchRequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public BatchRequestItemProcessor batchRequestItemProcessor() {
        return new BatchRequestItemProcessor();
    }

    @Bean
    public BatchRequestRetryItemProcessor batchRequestRetryItemProcessor() {

        return new BatchRequestRetryItemProcessor();
    }

    @Bean
    public BatchRequestStepListener batchRequestStepListener() {

        return new BatchRequestStepListener(batchRequestRepository, batchRequestItemRepository);
    }

    @Bean
    public BatchRequestRetryJobListener batchRequestRetryJobListener() {

        return new BatchRequestRetryJobListener();
    }

    @Bean
    public BatchRequestItemWriter writer() {
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

    public RepositoryItemReader<BatchRequestEntityItem> retryItemReader() {
        RepositoryItemReader<BatchRequestEntityItem> reader = new RepositoryItemReader<>();
        reader.setRepository(batchRequestItemRepository);
        reader.setMethodName("findAllByBatchRequestItemExecutionStatus");
        reader.setArguments(Collections.singletonList(BatchRequestItemExecutionStatus.RETRY));
        reader.setPageSize(10);
        Map<String, Sort.Direction> map = new HashMap<>();
        //TODO: Sort by least retry Delay
        map.put("id", Sort.Direction.ASC);
        reader.setSort(map);
        return reader;
    }

    @Bean
    public Step processBatchStep() {

        return stepBuilderFactory.get("batch-step").<BatchRequestEntityItem, BatchRequestEntityItem>chunk(2)
              .reader(itemReader(null)).processor(batchRequestItemProcessor()).writer(writer()).taskExecutor(taskExecutor())
              .listener(batchRequestStepListener()).build();
    }

    @Bean
    public Step processRetryBatchStep() {

        return stepBuilderFactory.get("retry-batch-step").<BatchRequestEntityItem, BatchRequestEntityItem>chunk(2)
              .reader(retryItemReader()).processor(batchRequestRetryItemProcessor()).writer(writer()).taskExecutor(taskExecutor())
              .listener(batchRequestStepListener()).build();
    }

    @Bean(name="batchJob")
    public Job runJob() {
        return jobBuilderFactory.get("processBatch")
                .flow(processBatchStep()).end().build();

    }

    @Bean(name="retryJob")
    public Job runRetryJob() {
        return jobBuilderFactory.get("processRetryBatch")
              .listener(batchRequestRetryJobListener())
              .flow(processRetryBatchStep()).end().build();

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
