package com.javatechie.spring.batch.controller;

import com.javatechie.spring.batch.dto.BatchResponse;
import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.service.impl.JobExecutionService;
import com.javatechie.spring.batch.service.impl.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/jobs")
public class JobController {

   @Autowired
   private JobService jobService;

   @Autowired
   private JobExecutionService jobExecutionService;

   @GetMapping("status")
   public String getJobData(@RequestParam("executionId") long id) {

      return jobService.getJobExecutionStatusByJobId(id);
   }

   @GetMapping("test")
   public String test() {

      return "Working. Time - " + new Date().toString();
   }

   @GetMapping("details")
   public ResponseEntity<BatchResponse> getJobDetailsByJobId(@RequestParam("batch_id") long id) {

      BatchResponse response = jobService.getJobDetailsByJobId(id);
      return ResponseEntity.ok(response);
   }

   //TODO: This will be triggered using scheduler
   @PostMapping
   public ResponseEntity<String> executeJob() {

      BatchRequestEntity batchRequestEntity = jobExecutionService.executeBatchRequest();
      Long jobExecutionId = batchRequestEntity == null ? null : batchRequestEntity.getJobExecutionId();
      return ResponseEntity.ok("Job with execution ID: " + jobExecutionId + " has been submitted for processing.");
   }
}
