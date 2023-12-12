package com.javatechie.spring.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class CreateInventoryRequest implements Serializable {

   private String grn;
   private int atp;
   private boolean inTransit;
   private int quantity;
}
