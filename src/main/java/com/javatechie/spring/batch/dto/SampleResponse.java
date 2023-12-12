package com.javatechie.spring.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SampleResponse implements Serializable {
   private List<SampleResponseItem> sampleResponseItems;
}
