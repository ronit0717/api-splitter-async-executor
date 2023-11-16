package com.javatechie.spring.batch.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class JobRetryService {

   @Autowired
   @Qualifier("batchJobLauncher")
   private JobLauncher jobLauncher;

   @Autowired
   @Qualifier("retryJob")
   private Job job;

   @Transactional(propagation = Propagation.NOT_SUPPORTED)
   public JobExecution processRetryBatch() {

      JobParameters jobParameters = new JobParametersBuilder().addLong("retry_job_timestamp", new Date().getTime())
            .toJobParameters();
      JobExecution jobExecution = null;
      try {
         jobExecution = jobLauncher.run(job, jobParameters);
      } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
               JobParametersInvalidException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
         throw new RuntimeException("Process Retry Batch failed with error", e);
      }
      return jobExecution;
   }
}
