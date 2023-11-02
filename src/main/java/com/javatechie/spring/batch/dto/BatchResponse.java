package com.javatechie.spring.batch.dto;

import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Data
public class BatchResponse {
   private Long jobExecutionId;
   private String jobStatus;
   private List<BatchEntity> batchItems;

   public BatchResponse(JobExecution jobExecution, BatchRequestEntity entity, Page<BatchRequestEntityItem> entityItems) {
      this.jobExecutionId = entity.getJobExecutionId();
      this.jobStatus = jobExecution.getStatus().toString();
      List<BatchRequestEntityItem> items = entityItems.getContent();
      this.batchItems = new LinkedList<>();
      for (BatchRequestEntityItem item : items) {
         BatchEntity batchEntity = new BatchEntity(item);
         this.batchItems.add(batchEntity);
      }
   }

}
