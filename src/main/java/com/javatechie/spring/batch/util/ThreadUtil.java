package com.javatechie.spring.batch.util;

public class ThreadUtil {

   public static void sleep(long millis) {
      try {
         Thread.sleep(millis);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
}
