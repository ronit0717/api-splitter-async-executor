package com.javatechie.spring.batch.service;

import com.javatechie.spring.batch.dto.CreateInventoryRequest;
import com.javatechie.spring.batch.entity.BatchRequestEntity;

public interface InventoryService {

      BatchRequestEntity createInventoryBulk(CreateInventoryRequest request);
      void createInventory(CreateInventoryRequest request);

      void updateInventory();

      void inventoryVariance();
}
