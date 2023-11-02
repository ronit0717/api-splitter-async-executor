package com.javatechie.spring.batch.service;

import com.javatechie.spring.batch.dto.BatchRequest;
import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.repository.BatchRequestItemRepository;
import com.javatechie.spring.batch.repository.BatchRequestRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BatchService {

   @Autowired
   private BatchRequestRepository batchRequestRepository;

   @Autowired
   private BatchRequestItemRepository batchRequestItemRepository;

   @Autowired
   @Qualifier("batchJobLauncher")
   private JobLauncher jobLauncher;
   @Autowired
   private Job job;

   @Transactional(propagation = Propagation.NOT_SUPPORTED)
   public BatchRequestEntity process(BatchRequest batchRequest) {

      BatchRequestEntity batchRequestEntity =  batchRequestRepository.save(batchRequest.getBatchRequestEntity());
      for (BatchRequestEntityItem item : batchRequest.getBatchRequestEntityItems()) {
         item.setBatchRequestId(batchRequestEntity.getId());
      }
      batchRequestItemRepository.saveAll(batchRequest.getBatchRequestEntityItems());
      long jobExecutionId = processBatch(batchRequestEntity);
      batchRequestEntity.setJobExecutionId(jobExecutionId);
      batchRequestEntity = batchRequestRepository.save(batchRequest.getBatchRequestEntity());
      return batchRequestEntity;
   }

   private long processBatch(BatchRequestEntity batchRequest) {

      JobParameters jobParameters = new JobParametersBuilder().addLong("batch_request_id", batchRequest.getId())
            .toJobParameters();
      JobExecution jobExecution = null;
      try {
         jobExecution = jobLauncher.run(job, jobParameters);
      } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
               JobParametersInvalidException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
         throw new RuntimeException("Process Batch failed with error", e);
      }
      return jobExecution.getId();
   }

}
