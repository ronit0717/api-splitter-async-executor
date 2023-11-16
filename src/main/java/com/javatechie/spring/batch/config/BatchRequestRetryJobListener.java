package com.javatechie.spring.batch.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class BatchRequestRetryJobListener implements JobExecutionListener {

   private JobExecution _active;

   @Override
   public void beforeJob(JobExecution jobExecution) {

      System.out.println("Start of retry job");
      //create a lock
      synchronized (jobExecution) {
         if (_active != null && _active.isRunning()) {
            jobExecution.stop();
         } else {
            _active = jobExecution;
         }
      }
   }

   @Override
   public void afterJob(JobExecution jobExecution) {

      System.out.println("End of retry job");
      //release the lock
      synchronized (jobExecution) {
         if (jobExecution == _active) {
            _active = null;
         }
      }
   }
}
