package com.emergencymed.request.repository;

import com.emergencymed.request.entity.RequestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistory, Long> {
    List<RequestStatusHistory> findByRequestIdOrderByCreatedAtAsc(Long requestId);
}
