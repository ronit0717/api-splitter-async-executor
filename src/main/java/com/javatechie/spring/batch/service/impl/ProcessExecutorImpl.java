package com.javatechie.spring.batch.service.impl;

import com.javatechie.spring.batch.dto.CreateInventoryRequest;
import com.javatechie.spring.batch.dto.ExecuteProcessRequestWrapper;
import com.javatechie.spring.batch.dto.ExecuteProcessResponseWrapper;
import com.javatechie.spring.batch.dto.SampleRequest;
import com.javatechie.spring.batch.dto.SampleResponse;
import com.javatechie.spring.batch.enumeration.BatchRequestItemExecutionStatus;
import com.javatechie.spring.batch.enumeration.RequestType;
import com.javatechie.spring.batch.service.InventoryService;
import com.javatechie.spring.batch.service.ProcessExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

@Service
public class ProcessExecutorImpl implements ProcessExecutor {

   @Autowired
   private InventoryService inventoryService;

   @Autowired
   private SampleBulkService sampleService;

   @Override
   public ExecuteProcessResponseWrapper executeProcess(ExecuteProcessRequestWrapper request) {

      RequestType requestType = RequestType.valueOf(request.getRequestType());
      ExecuteProcessResponseWrapper responseWrapper = null;
      byte[] response = null;
      try {
         switch (requestType) {
         case CREATE_INVENTORY:
            CreateInventoryRequest createInventoryRequest = (CreateInventoryRequest) SerializationUtils
                  .deserialize(request.getRequest());
            inventoryService.createInventory(createInventoryRequest);
            break;
         case UPDATE_INVENTORY:
            inventoryService.updateInventory();
            break;
         case INVENTORY_VARIANCE:
            inventoryService.inventoryVariance();
            break;
         case SAMPLE_OPERATION:
            SampleRequest sampleRequest = (SampleRequest) SerializationUtils.deserialize(request.getRequest());
            SampleResponse sampleResponse = sampleService.executeChunkSampleRequest(sampleRequest);
            response = sampleResponse == null ? null : SerializationUtils.serialize(sampleResponse);
         }
         responseWrapper = new ExecuteProcessResponseWrapper(BatchRequestItemExecutionStatus.SUCCESS, response);
      } catch (Exception e) {
         e.printStackTrace();
         responseWrapper = new ExecuteProcessResponseWrapper(BatchRequestItemExecutionStatus.ERROR,
               null); //TODO: give proper error message
      }
      return responseWrapper;
   }
}
