package com.javatechie.spring.batch.repository;

import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.enumeration.BatchRequestExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRequestRepository extends JpaRepository<BatchRequestEntity, Long> {

   Page<BatchRequestEntity> findAllByBatchRequestExecutionStatusAndIsLocked(
         BatchRequestExecutionStatus batchRequestExecutionStatus, boolean isLocked, Pageable pageable);

}
