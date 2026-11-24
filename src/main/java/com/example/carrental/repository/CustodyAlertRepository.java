package com.example.carrental.repository;

import com.example.carrental.entity.CustodyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustodyAlertRepository extends JpaRepository<CustodyAlert, Long> {
    List<CustodyAlert> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
    List<CustodyAlert> findByResolvedFalseOrderByCreatedAtDesc();
}
