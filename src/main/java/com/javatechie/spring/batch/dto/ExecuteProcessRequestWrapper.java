package com.javatechie.spring.batch.dto;

import com.javatechie.spring.batch.enumeration.RequestType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class ExecuteProcessRequestWrapper implements Serializable {

   private String requestType;
   private byte[] request;
}
