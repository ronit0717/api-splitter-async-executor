package com.javatechie.spring.batch.dto;

import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.enumeration.ExecutionStatus;
import com.javatechie.spring.batch.enumeration.HttpRequestMethod;
import lombok.Data;

import java.util.Date;

@Data
public class BatchEntity {
   private long id;
   private HttpRequestMethod httpRequestMethod;
   private String httpRequestUri;
   private String httpRequestHeader;
   private String httpRequestBody;
   private ExecutionStatus executionStatus;
   private String httpResponseCode;
   private String httpResponseHeader;
   private String httpResponseBody;
   private Date createdOn;
   private Date updatedOn;

   public BatchEntity(BatchRequestEntityItem item) {
      this.id = item.getId();
      this.httpRequestMethod = item.getHttpRequestMethod();
      this.httpRequestUri = item.getHttpRequestUri();
      this.httpRequestHeader = item.getHttpRequestHeader();
      this.httpRequestBody = item.getHttpRequestBody();
      this.executionStatus = item.getExecutionStatus();
      this.httpResponseCode = item.getHttpResponseCode();
      this.httpResponseHeader = item.getHttpResponseHeader();
      this.httpResponseBody = item.getHttpResponseBody();
      this.createdOn = item.getCreatedOn();
      this.updatedOn = item.getUpdatedOn();
   }
}
