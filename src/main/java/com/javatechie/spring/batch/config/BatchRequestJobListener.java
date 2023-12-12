package com.javatechie.spring.batch.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class BatchRequestJobListener implements JobExecutionListener {

   private JobExecution _active;

   /**
    * This method is called before job starts. This ensures that only one job is being executed at a time.
    * The method acquires a lock before the start of the job.
    * @param jobExecution
    */
   @Override
   public void beforeJob(JobExecution jobExecution) {

      System.out.println("Start of job execution");
      //create a lock
      synchronized (jobExecution) {
         if (_active != null && _active.isRunning()) {
            jobExecution.stop();
         } else {
            _active = jobExecution;
         }
      }
   }

   /**
    * This method is called after job ends. This releases the lock.
    * @param jobExecution
    */
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
