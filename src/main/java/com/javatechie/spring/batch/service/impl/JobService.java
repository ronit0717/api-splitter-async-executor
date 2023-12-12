package com.javatechie.spring.batch.service.impl;

import com.javatechie.spring.batch.dto.BatchRequest;
import com.javatechie.spring.batch.dto.BatchResponse;
import com.javatechie.spring.batch.dto.CreateInventoryRequest;
import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.enumeration.BatchRequestExecutionStatus;
import com.javatechie.spring.batch.enumeration.BatchRequestItemExecutionStatus;
import com.javatechie.spring.batch.repository.BatchRequestItemRepository;
import com.javatechie.spring.batch.repository.BatchRequestRepository;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.SerializationUtils;

import java.util.LinkedList;
import java.util.List;

import static com.javatechie.spring.batch.enumeration.RequestType.CREATE_INVENTORY;

@Service
public class JobService<T> {

   @Autowired
   private JobExplorer jobExplorer;

   @Autowired
   private BatchRequestItemRepository batchRequestItemRepository;

   @Autowired
   private BatchRequestRepository batchRequestRepository;

   @Transactional(propagation = Propagation.NOT_SUPPORTED)
   public BatchRequestEntity createJob(List<T> requests, String requestType) {

      BatchRequest batchRequest = buildBatchRequest(requests, requestType);
      BatchRequestEntity batchRequestEntity =  batchRequestRepository.save(batchRequest.getBatchRequestEntity());
      for (BatchRequestEntityItem item : batchRequest.getBatchRequestEntityItems()) {
         item.setBatchRequestId(batchRequestEntity.getId());
      }
      batchRequestItemRepository.saveAll(batchRequest.getBatchRequestEntityItems());
      return batchRequestEntity;
   }

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

   //private helper methods
   private BatchRequest buildBatchRequest(List<T> requests, String requestType) {

      List<BatchRequestEntityItem> batchRequestEntityItems = null;
      BatchRequestEntity batchRequestEntity = BatchRequestEntity.builder()
            .batchRequestExecutionStatus(BatchRequestExecutionStatus.IN_PROGRESS).build();
      List<byte[]> serializedRequests = null;
      try {
         serializedRequests = serializeRequests(requests);
         for (byte[] request : serializedRequests) {
            if (batchRequestEntityItems == null) {
               batchRequestEntityItems = new LinkedList<>();
            }
            BatchRequestEntityItem batchRequestEntityItem = BatchRequestEntityItem.builder()
                  .batchRequestItemExecutionStatus(BatchRequestItemExecutionStatus.PENDING)
                  .requestType(requestType)
                  .request(request).build();
            batchRequestEntityItems.add(batchRequestEntityItem);
         }
         return BatchRequest.builder().batchRequestEntity(batchRequestEntity)
               .batchRequestEntityItems(batchRequestEntityItems).build();
      } catch (Exception e) {
         throw new RuntimeException("Request processing failed with error " + e.getMessage());
      }
   }

   private List<byte[]> serializeRequests(List<T> requests) throws Exception {
      List<byte[]> serializedRequests = new LinkedList<>();
      for (Object request : requests) {
         byte[] serializedRequest = SerializationUtils.serialize(request);
         serializedRequests.add(serializedRequest);
      }
      return serializedRequests;
   }

}
