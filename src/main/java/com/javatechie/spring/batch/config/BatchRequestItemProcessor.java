package com.javatechie.spring.batch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.spring.batch.dto.ExecuteProcessRequestWrapper;
import com.javatechie.spring.batch.dto.ExecuteProcessResponseWrapper;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.service.ProcessExecutor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class BatchRequestItemProcessor implements ItemProcessor<BatchRequestEntityItem, BatchRequestEntityItem> {

   @Autowired
   ObjectMapper objectMapper;

   @Autowired
   ProcessExecutor executor;

   @Override
   public BatchRequestEntityItem process(BatchRequestEntityItem item) throws Exception {
      long threadId = Thread.currentThread().getId();
      long itemId = item.getId();
      String message = String.format("Thread: %s, Processing item with id: %s", threadId, itemId);
      System.out.println(message);
      //TODO: Integrate logging

      //TODO: Logic to filter out items which is being retried without a delay

      ExecuteProcessRequestWrapper requestWrapper = new ExecuteProcessRequestWrapper(item.getRequestType(), item.getRequest());
      ExecuteProcessResponseWrapper responseWrapper = executor.executeProcess(requestWrapper);
      item.setResponse(responseWrapper.getResponse());
      item.setBatchRequestItemExecutionStatus(responseWrapper.getStatus());

      //TODO: Retry logic handling

      return item;
   }
}
