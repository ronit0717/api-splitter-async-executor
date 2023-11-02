package com.javatechie.spring.batch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.spring.batch.dto.BatchRequest;
import com.javatechie.spring.batch.dto.SampleRequest;
import com.javatechie.spring.batch.dto.SampleRequestItem;
import com.javatechie.spring.batch.dto.SampleResponse;
import com.javatechie.spring.batch.dto.SampleResponseItem;
import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.enumeration.ExecutionStatus;
import com.javatechie.spring.batch.enumeration.HttpRequestMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Service
public class SampleBulkService {

   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private BatchService batchService;

   private final int PAYLOAD_MAX_SIZE = 3;

   public BatchRequestEntity executeBulkSampleRequest(SampleRequest sampleRequest) {

      if (CollectionUtils.isEmpty(sampleRequest.getSampleRequestItems())) {
         return null;
      }
      List<SampleRequest> splitSampleRequests = splitSampleRequest(sampleRequest, PAYLOAD_MAX_SIZE);
      BatchRequest batchRequest = buildBatchRequest(splitSampleRequests);
      return batchService.process(batchRequest);
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
      try {
         Thread.sleep(sleepTimeInMilliseconds);
      } catch (InterruptedException ie) {
         Thread.currentThread().interrupt();
      }
   }

   private BatchRequest buildBatchRequest(List<SampleRequest> sampleRequests) {

      List<BatchRequestEntityItem> batchRequestEntityItems = null;
      BatchRequestEntity batchRequestEntity = BatchRequestEntity.builder().build();
      List<String> stringSampleRequests = null;
      try {
         stringSampleRequests = stringify(sampleRequests);
         String headers = objectMapper.writeValueAsString(buildHeaders());
         for (String payload : stringSampleRequests) {
            if (batchRequestEntityItems == null) {
               batchRequestEntityItems = new LinkedList<>();
            }
            BatchRequestEntityItem batchRequestEntityItem = BatchRequestEntityItem.builder()
                  .httpRequestMethod(HttpRequestMethod.POST)
                  .httpRequestUri("http://localhost:9191/sample/process/chunk").httpRequestHeader(headers)
                  .executionStatus(ExecutionStatus.PENDING).httpRequestBody(payload).build();
            batchRequestEntityItems.add(batchRequestEntityItem);
         }
         return BatchRequest.builder().batchRequestEntity(batchRequestEntity)
               .batchRequestEntityItems(batchRequestEntityItems).build();
      } catch (Exception e) {
         throw new RuntimeException("Request processing failed with error " + e.getMessage());
      }
   }

   private HttpHeaders buildHeaders() {

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      return headers;
   }

   private List<String> stringify(List<SampleRequest> sampleRequests) throws JsonProcessingException {
      List<String> stringSampleRequests = new LinkedList<>();
      for (SampleRequest sampleRequest : sampleRequests) {
         String requestString = objectMapper.writeValueAsString(sampleRequest);
         stringSampleRequests.add(requestString);
      }
      return stringSampleRequests;
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
