package com.javatechie.spring.batch.repository;

import com.javatechie.spring.batch.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory,Integer> {
}
