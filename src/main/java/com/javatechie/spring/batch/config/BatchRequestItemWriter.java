package com.javatechie.spring.batch.config;

import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.repository.BatchRequestItemRepository;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.data.RepositoryItemWriter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BatchRequestItemWriter extends RepositoryItemWriter<BatchRequestEntityItem> {

   private StepExecution stepExecution;

   public BatchRequestItemWriter(BatchRequestItemRepository batchRequestItemRepository) {

      super();
      setRepository(batchRequestItemRepository);
      setMethodName("save");
   }

   @BeforeStep
   public void initStepExecution(StepExecution stepExecution) {

      this.stepExecution = stepExecution;
   }

   @Override
   public void write(List<? extends BatchRequestEntityItem> items) throws Exception {

      System.out.println("Custom write called");
      super.write(items);
      updateStepContextWithBatchEntityIds(items);
   }

   private void updateStepContextWithBatchEntityIds(List<? extends BatchRequestEntityItem> items) {

      ExecutionContext stepContext = this.stepExecution.getExecutionContext();
      Set<Long> batchEntityIds = (Set<Long>)stepContext.get("batchEntityIds");
      Set<Long> newBatchEntityIds = items.stream().map(BatchRequestEntityItem::getBatchRequestId)
            .collect(Collectors.toSet());
      if (batchEntityIds == null) {
         batchEntityIds = new HashSet<>();
      }
      batchEntityIds.addAll(newBatchEntityIds);
      stepContext.put("batchEntityIds", batchEntityIds);
   }
}
