package com.example.carrental.repository;

import com.example.carrental.entity.VehicleInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleInspectionRepository extends JpaRepository<VehicleInspection, Long> {
    List<VehicleInspection> findByBookingIdOrderByInspectedAtAsc(Long bookingId);
}
