package com.javatechie.spring.batch.dto;

import com.javatechie.spring.batch.entity.BatchRequestEntity;
import com.javatechie.spring.batch.entity.BatchRequestEntityItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchRequest {
   private BatchRequestEntity batchRequestEntity;
   private List<BatchRequestEntityItem> batchRequestEntityItems;
}
