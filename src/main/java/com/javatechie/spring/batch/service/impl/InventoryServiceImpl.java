package com.javatechie.spring.batch.service.impl;

import com.javatechie.spring.batch.dto.BatchRequest;
import com.javatechie.spring.batch.dto.CreateInventoryRequest;
import com.javatechie.spring.batch.dto.SampleRequest;
import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import com.javatechie.spring.batch.entity.Inventory;
import com.javatechie.spring.batch.enumeration.BatchRequestExecutionStatus;
import com.javatechie.spring.batch.enumeration.BatchRequestItemExecutionStatus;
import com.javatechie.spring.batch.repository.InventoryRepository;
import com.javatechie.spring.batch.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.util.LinkedList;
import java.util.List;

import static com.javatechie.spring.batch.enumeration.RequestType.CREATE_INVENTORY;
import static com.javatechie.spring.batch.enumeration.RequestType.SAMPLE_OPERATION;
import static com.javatechie.spring.batch.util.ThreadUtil.sleep;

@Service
public class InventoryServiceImpl implements InventoryService {

   @Autowired
   private JobService jobService;

   @Autowired
   private InventoryRepository inventoryRepository;

   private final int MAX_INVENTORY_LIMIT = 20;

   @Override
   public BatchRequestEntity createInventoryBulk(CreateInventoryRequest request) {
      List<CreateInventoryRequest> createInventoryRequests = splitInventoryCreateRequest(request);
      return jobService.createJob(createInventoryRequests, CREATE_INVENTORY.name());
   }

   /**
    * Create inventory (non-bulk implementation)
    * @param request
    */
   @Override
   public void createInventory(CreateInventoryRequest request) {

      if (request.getQuantity() > MAX_INVENTORY_LIMIT) {
         throw new RuntimeException("Inventory limit exceeded");
      }
      List<Inventory> inventories = new LinkedList<>();
      for (int i = 0; i < request.getQuantity(); i++) {
         Inventory inventory = new Inventory(request.getGrn(), request.getAtp(), request.isInTransit());
         inventories.add(inventory);
      }
      inventoryRepository.saveAll(inventories);
      sleep(2000L); //for simulation
   }

   @Override
   public void updateInventory() {

   }

   @Override
   public void inventoryVariance() {

   }

   private List<CreateInventoryRequest> splitInventoryCreateRequest(CreateInventoryRequest request) {

      List<CreateInventoryRequest> splitRequests = new LinkedList<>();
      int quantity = request.getQuantity();
      int i = 0;
      while (quantity > 0) {
         CreateInventoryRequest splitRequest = new CreateInventoryRequest(request.getGrn(), request.getAtp(),
               request.isInTransit(), quantity > MAX_INVENTORY_LIMIT ? MAX_INVENTORY_LIMIT : quantity);
         splitRequests.add(splitRequest);
         quantity -= MAX_INVENTORY_LIMIT;
      }
      return splitRequests;
   }

}
