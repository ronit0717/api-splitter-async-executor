package com.javatechie.spring.batch.service;

import com.javatechie.spring.batch.dto.ExecuteProcessRequestWrapper;
import com.javatechie.spring.batch.dto.ExecuteProcessResponseWrapper;

public interface ProcessExecutor {

   ExecuteProcessResponseWrapper executeProcess(ExecuteProcessRequestWrapper request);
}
