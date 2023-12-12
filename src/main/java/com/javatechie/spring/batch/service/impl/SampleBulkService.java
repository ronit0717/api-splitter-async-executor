package com.javatechie.spring.batch.service.impl;

import com.javatechie.spring.batch.dto.BatchRequest;
import com.javatechie.spring.batch.dto.SampleRequest;
import com.javatechie.spring.batch.dto.SampleRequestItem;
import com.javatechie.spring.batch.dto.SampleResponse;
import com.javatechie.spring.batch.dto.SampleResponseItem;
import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.enumeration.BatchRequestExecutionStatus;
import com.javatechie.spring.batch.enumeration.BatchRequestItemExecutionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.SerializationUtils;

import java.util.LinkedList;
import java.util.List;

import static com.javatechie.spring.batch.enumeration.RequestType.SAMPLE_OPERATION;
import static com.javatechie.spring.batch.util.ThreadUtil.sleep;

@Service
public class SampleBulkService {

   @Autowired
   private JobService jobService;

   private final int PAYLOAD_MAX_SIZE = 3;

   public BatchRequestEntity executeBulkSampleRequest(SampleRequest sampleRequest) {

      if (CollectionUtils.isEmpty(sampleRequest.getSampleRequestItems())) {
         return null;
      }
      List<SampleRequest> splitSampleRequests = splitSampleRequest(sampleRequest, PAYLOAD_MAX_SIZE);
      return jobService.createJob(splitSampleRequests, SAMPLE_OPERATION.name());
   }

   public SampleResponse executeChunkSampleRequest(SampleRequest sampleRequest) {

      try {
         List<SampleResponseItem> sampleResponseItems = null;
         for (SampleRequestItem item : sampleRequest.getSampleRequestItems()) {
            if (sampleResponseItems == null) {
               sampleResponseItems = new LinkedList<>();
            }
            SampleResponseItem responseItem = SampleResponseItem.builder()
                  .ldap((item.getFirstName() + "." + item.getLastName()).toLowerCase()).build();
            pauseExecution(responseItem); //Temp Code, added this to test asynchrounous process
            sampleResponseItems.add(responseItem);
         }
         return SampleResponse.builder().sampleResponseItems(sampleResponseItems).build();
      } catch (Exception e) {
         throw new RuntimeException("Execution failed with error: " + e.getMessage());
      }
   }

   //Temporary method added to test asynchronous processing
   private void pauseExecution(SampleResponseItem sampleResponseItem) {

      int sleepTimeInMilliseconds = (sampleResponseItem.getLdap().length() % 2 == 0 ) ? 2000 : 1000;
      sleep(sleepTimeInMilliseconds);
   }

   private List<SampleRequest> splitSampleRequest(SampleRequest sampleRequest, int maxSize) {

      if (maxSize < 1) {
         throw new RuntimeException("max size should be greater than 0");
      }
      List<SampleRequest> splitRequests = new LinkedList<>();
      SampleRequest splitRequest = null;
      int counter = 0;
      for(SampleRequestItem item : sampleRequest.getSampleRequestItems()) {
         if (counter % maxSize == 0) {
            if (counter != 0 ) splitRequests.add(splitRequest);
            splitRequest = new SampleRequest();
         }
         counter++;
         List<SampleRequestItem> sampleRequestItems =
               splitRequest.getSampleRequestItems() == null ? new LinkedList<>() : splitRequest.getSampleRequestItems();
         sampleRequestItems.add(item);
         splitRequest.setSampleRequestItems(sampleRequestItems);
      }
      if (splitRequest != null && !CollectionUtils.isEmpty(splitRequest.getSampleRequestItems())) {
         splitRequests.add(splitRequest);
      }
      return splitRequests;
   }

}
