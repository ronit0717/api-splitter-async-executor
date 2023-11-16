package com.javatechie.spring.batch.config;

import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.enumeration.BatchRequestExecutionStatus;
import com.javatechie.spring.batch.enumeration.BatchRequestItemExecutionStatus;
import com.javatechie.spring.batch.repository.BatchRequestItemRepository;
import com.javatechie.spring.batch.repository.BatchRequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
public class BatchRequestStepListener implements StepExecutionListener {

   private BatchRequestRepository batchRequestRepository;
   private BatchRequestItemRepository batchRequestItemRepository;

   @Override
   public void beforeStep(StepExecution stepExecution) {

   }

   @Override
   public ExitStatus afterStep(StepExecution stepExecution) {

      Object batchIds = stepExecution.getExecutionContext().get("batchEntityIds");
      if (batchIds != null) {
         validateItemStatusAndUpdateBatchRequest((Set<Long>) batchIds);
      }
      return ExitStatus.COMPLETED;
   }

   private void validateItemStatusAndUpdateBatchRequest(Set<Long> batchRequestIds) {

      if (CollectionUtils.isEmpty(batchRequestIds)) {
         return;
      }
      for (Long batchRequestId : batchRequestIds) {
         Pageable pageable = Pageable.unpaged();
         Page<BatchRequestEntityItem> batchRequestEntityItemList = batchRequestItemRepository.findAllByBatchRequestId(
               batchRequestId, pageable);
         List<BatchRequestEntityItem> batchRequestEntityItems = batchRequestEntityItemList.getContent();
         BatchRequestExecutionStatus status = BatchRequestExecutionStatus.COMPLETED;
         //TODO: Make logic efficient (prevent iterative approach)
         for (BatchRequestEntityItem batchRequestEntityItem : batchRequestEntityItems) {
            if (batchRequestEntityItem.getBatchRequestItemExecutionStatus() == BatchRequestItemExecutionStatus.ERROR) {
               status = BatchRequestExecutionStatus.FAILED;
               break;
            } else if (
                  batchRequestEntityItem.getBatchRequestItemExecutionStatus() == BatchRequestItemExecutionStatus.PENDING
                        || batchRequestEntityItem.getBatchRequestItemExecutionStatus()
                        == BatchRequestItemExecutionStatus.RETRY) {
               status = BatchRequestExecutionStatus.IN_PROGRESS;
            }
         }
         Optional<BatchRequestEntity> optionalBatchRequestEntity = batchRequestRepository.findById(batchRequestId);
         if (optionalBatchRequestEntity.isPresent()) {
            BatchRequestEntity batchRequestEntity = optionalBatchRequestEntity.get();
            batchRequestEntity.setBatchRequestExecutionStatus(status);
            batchRequestRepository.save(batchRequestEntity);
         }
         //TODO: Callback logic
      }
   }
}
