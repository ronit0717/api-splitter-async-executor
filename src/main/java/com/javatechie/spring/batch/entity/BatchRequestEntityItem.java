package com.javatechie.spring.batch.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.javatechie.spring.batch.enumeration.BatchRequestItemExecutionStatus;
import com.javatechie.spring.batch.enumeration.HttpRequestMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "batch_request_items")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"createdOn", "updatedOn"}, allowGetters = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchRequestEntityItem {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long id;

   @Column(nullable = false)
   private long batchRequestId; //Foreign Key

   @Column(length = 10, nullable = false)
   @Enumerated(value = EnumType.STRING)
   private HttpRequestMethod httpRequestMethod;

   @Column(length = 4096, nullable = false)
   private String httpRequestUri;

   @Column(columnDefinition = "TEXT")
   private String httpRequestHeader;

   @Column
   private Integer retryDelay;

   @Column(columnDefinition = "TEXT", nullable = false)
   private String httpRequestBody;

   @Column(name = "status", length = 15, nullable = false)
   @Enumerated(value = EnumType.STRING)
   private BatchRequestItemExecutionStatus batchRequestItemExecutionStatus;

   @Column(length = 3)
   private String httpResponseCode;

   @Column(columnDefinition = "TEXT")
   private String httpResponseHeader;

   @Column(columnDefinition = "TEXT")
   private String httpResponseBody;

   @Column(nullable = false, updatable = false)
   @Temporal(TemporalType.TIMESTAMP)
   @CreatedDate
   private Date createdOn;

   @Column(nullable = false)
   @Temporal(TemporalType.TIMESTAMP)
   @LastModifiedDate
   private Date updatedOn;

}
