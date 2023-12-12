package com.javatechie.spring.batch.service.impl;

import com.javatechie.spring.batch.dto.BatchRequest;
import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.enumeration.BatchRequestExecutionStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JobExecutionService {

   @Autowired
   private BatchRequestRepository batchRequestRepository;

   @Autowired
   private BatchRequestItemRepository batchRequestItemRepository;
   @Autowired
   @Qualifier("batchJobLauncher")
   private JobLauncher jobLauncher;
   @Autowired
   @Qualifier("bulkProcessingJob")
   private Job job;

   @Transactional(propagation = Propagation.NOT_SUPPORTED)
   public BatchRequestEntity executeBatchRequest() {
      //Find batchRequestEntities with status as IN PROGRESS SORTED BY id ASC LIMIT 10
      Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
      Page<BatchRequestEntity> batchRequestEntities =
            batchRequestRepository.findAllByBatchRequestExecutionStatusAndIsLocked(
            BatchRequestExecutionStatus.IN_PROGRESS, false, pageable);
      if (batchRequestEntities.isEmpty()) {
         return null;
      }
      BatchRequestEntity batchRequestEntity = selectAndLockBatchRequestEntityForExecution(
            batchRequestEntities.getContent());

      long jobExecutionId = processBatch(batchRequestEntity);
      batchRequestEntity.setJobExecutionId(jobExecutionId);
      return batchRequestRepository.save(batchRequestEntity);
   }

   private BatchRequestEntity selectAndLockBatchRequestEntityForExecution(List<BatchRequestEntity> batchRequestEntities) {
      final int MAX_RETRY_COUNT = 10;
      Set<Integer> processedIndex = new HashSet<>();
      int retryCount = 0;
      BatchRequestEntity batchRequestEntity = null;
      while (retryCount < MAX_RETRY_COUNT) {
         int randomIndex = (int) (Math.random() * batchRequestEntities.size());
         batchRequestEntity = batchRequestEntities.get(randomIndex);
         batchRequestEntity.setLocked(true);
         batchRequestEntity = batchRequestRepository.save(batchRequestEntity); //TODO: Logic to prevent dirty write
         if (batchRequestEntity.isLocked()) {
            break;
         } else {
            retryCount++;
         }
      }
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
