package com.javatechie.spring.batch.dto;

import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.enumeration.BatchRequestItemExecutionStatus;
import com.javatechie.spring.batch.enumeration.HttpRequestMethod;
import lombok.Data;

import java.util.Date;

@Data
public class BatchEntity {
   private long id;
   private HttpRequestMethod httpRequestMethod;
   private BatchRequestItemExecutionStatus batchRequestItemExecutionStatus;
   private Date createdOn;
   private Date updatedOn;

   public BatchEntity(BatchRequestEntityItem item) {
      this.id = item.getId();
      this.batchRequestItemExecutionStatus = item.getBatchRequestItemExecutionStatus();
      this.createdOn = item.getCreatedOn();
      this.updatedOn = item.getUpdatedOn();
   }
}
