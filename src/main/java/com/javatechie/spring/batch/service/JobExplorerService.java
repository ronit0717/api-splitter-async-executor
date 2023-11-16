package com.javatechie.spring.batch.service;

import com.javatechie.spring.batch.dto.BatchRequest;
import com.javatechie.spring.batch.dto.BatchResponse;
import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.repository.BatchRequestItemRepository;
import com.javatechie.spring.batch.repository.BatchRequestRepository;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class JobExplorerService {

   @Autowired
   private JobExplorer jobExplorer;

   @Autowired
   private BatchRequestItemRepository batchRequestItemRepository;

   @Autowired
   private BatchRequestRepository batchRequestRepository;

   public String getJobExecutionStatusByJobId(Long jobId) {

      JobExecution jobExecution = jobExplorer.getJobExecution(jobId);
      System.out.println(jobExecution.toString());
      return jobExecution.getStatus().toString();
   }

   //TODO: Add pageable support in interface
   public BatchResponse getJobDetailsByJobId(Long jobId) {

      BatchRequestEntity entity = batchRequestRepository.getById(jobId);
      JobExecution jobExecution = jobExplorer.getJobExecution(entity.getJobExecutionId());
      //Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
      Pageable pageable = Pageable.unpaged();
      Page<BatchRequestEntityItem> batchRequestEntityItems = batchRequestItemRepository.findAllByBatchRequestId(jobId,
            pageable);
      return new BatchResponse(jobExecution, entity, batchRequestEntityItems);
   }

}
