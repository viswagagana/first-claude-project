package com.payment.payment.repository;

import com.payment.payment.domain.OutboxEvent;

import java.util.List;

public interface OutboxEventRepository extends org.springframework.data.jpa.repository.JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByPublishedAtIsNullOrderByIdAsc(org.springframework.data.domain.Pageable pageable);
}
