package com.javatechie.spring.batch.controller;

import com.javatechie.spring.batch.dto.CreateInventoryRequest;
import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

   @Autowired
   private InventoryService service;

   @PostMapping()
   public ResponseEntity createInventoryBulk(@RequestBody CreateInventoryRequest request) {

      try {
         BatchRequestEntity inventoryCreateBatchRequest = service.createInventoryBulk(request);
         return ResponseEntity.ok(inventoryCreateBatchRequest);
      } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
      }

   }
}
