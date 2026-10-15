package com.emergencymed.request.repository;

import com.emergencymed.request.entity.RequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {
    List<RequestItem> findByRequestId(Long requestId);
}
