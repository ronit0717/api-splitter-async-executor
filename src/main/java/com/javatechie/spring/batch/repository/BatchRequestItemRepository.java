package com.javatechie.spring.batch.repository;

import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchRequestItemRepository extends JpaRepository<BatchRequestEntityItem, Long> {
   Page<BatchRequestEntityItem> findAllByBatchRequestId(long batchRequestId, Pageable pageable);
}
