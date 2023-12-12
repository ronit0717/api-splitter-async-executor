package com.javatechie.spring.batch.controller;

import com.javatechie.spring.batch.dto.SampleRequest;
import com.javatechie.spring.batch.dto.SampleResponse;
import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.service.impl.SampleBulkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample")
public class SampleController {

   @Autowired
   private SampleBulkService service;

   @PostMapping("process/bulk")
   public ResponseEntity processBulk(@RequestBody SampleRequest request) {
      try {
         BatchRequestEntity batchRequest = service.executeBulkSampleRequest(request);
         return ResponseEntity.ok(batchRequest);
      } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
      }

   }

   @PostMapping("process/chunk")
   public ResponseEntity processChunk(@RequestBody SampleRequest request) {
      try {
         SampleResponse response = service.executeChunkSampleRequest(request);
         return ResponseEntity.ok(response);
      } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
      }
   }

}
