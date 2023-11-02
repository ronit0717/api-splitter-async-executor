package com.javatechie.spring.batch.controller;

import com.javatechie.spring.batch.dto.BatchResponse;
import com.javatechie.spring.batch.service.JobExplorerService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
   private JobExplorerService jobExplorerService;

   @GetMapping("status")
   public String getJobData(@RequestParam("executionId") long id) {

      return jobExplorerService.getJobStatusByJobId(id);
   }

   @GetMapping("test")
   public String test() {

      return "Working. Time - " + new Date().toString();
   }

   @GetMapping("details")
   public ResponseEntity<BatchResponse> getJobDetailsByJobId(@RequestParam("batch_id") long id) {

      BatchResponse response = jobExplorerService.getJobDetailsByJobId(id);
      return ResponseEntity.ok(response);
   }
}
