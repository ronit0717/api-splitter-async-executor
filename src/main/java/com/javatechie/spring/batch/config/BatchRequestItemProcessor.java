package com.javatechie.spring.batch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.enumeration.BatchRequestItemExecutionStatus;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class BatchRequestItemProcessor implements ItemProcessor<BatchRequestEntityItem, BatchRequestEntityItem> {

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public BatchRequestEntityItem process(BatchRequestEntityItem item) throws Exception {
        long threadId = Thread.currentThread().getId();
        long itemId = item.getId();
        String message = String.format("Thread: %s, Processing item with id: %s", threadId, itemId);
        System.out.println(message);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = objectMapper.readValue(item.getHttpRequestHeader(), HttpHeaders.class);
        HttpEntity<?> entity = new HttpEntity(item.getHttpRequestBody(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(item.getHttpRequestUri(), entity, String.class);
        item.setHttpResponseCode(Integer.toString(response.getStatusCode().value()));
        item.setHttpResponseBody(response.getBody());
        item.setHttpResponseHeader(objectMapper.writeValueAsString(response.getHeaders()));

        //TODO: Retry logic handling

        if (response.getStatusCode().is2xxSuccessful()) {
//            if (item.getId() % 3 == 0) {
//                item.setBatchRequestItemExecutionStatus(BatchRequestItemExecutionStatus.RETRY);
//            } else {
//                item.setBatchRequestItemExecutionStatus(BatchRequestItemExecutionStatus.SUCCESS);
//            }
            item.setBatchRequestItemExecutionStatus(BatchRequestItemExecutionStatus.SUCCESS); //temp comment
        } else {
            item.setBatchRequestItemExecutionStatus(BatchRequestItemExecutionStatus.ERROR);
        }
        return item;
    }
}
