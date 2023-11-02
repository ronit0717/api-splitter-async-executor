package com.javatechie.spring.batch.repository;

import com.javatechie.spring.batch.entity.BatchRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRequestRepository extends JpaRepository<BatchRequestEntity, Long> {

}
