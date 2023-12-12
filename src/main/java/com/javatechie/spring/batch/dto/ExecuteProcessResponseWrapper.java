package com.javatechie.spring.batch.dto;

import com.javatechie.spring.batch.enumeration.BatchRequestItemExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class ExecuteProcessResponseWrapper implements Serializable {

   private BatchRequestItemExecutionStatus status;
   private byte[] response;
}
