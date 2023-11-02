package com.javatechie.spring.batch.config;

import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.entity.Customer;
import com.javatechie.spring.batch.repository.BatchRequestItemRepository;
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
    private BatchRequestItemRepository batchRequestItemRepository;

    @Autowired
    private JobRepository jobRepository;

    @Bean
    public BatchRequestItemProcessor processor() {
        return new BatchRequestItemProcessor();
    }

    @Bean
    public RepositoryItemWriter<BatchRequestEntityItem> writer() {
        RepositoryItemWriter<BatchRequestEntityItem> writer = new RepositoryItemWriter<>();
        writer.setRepository(batchRequestItemRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    @StepScope
    public RepositoryItemReader<BatchRequestEntityItem> reader(@Value("#{jobParameters[batch_request_id]}") Long batchRequestId) {
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
        return stepBuilderFactory.get("batch-step").<BatchRequestEntityItem, BatchRequestEntityItem>chunk(2)
                .reader(reader(null))
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job runJob() {
        return jobBuilderFactory.get("processBatch")
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
