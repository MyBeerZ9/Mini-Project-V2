package com.example.carrental.repository;

import com.example.carrental.entity.Car;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {

    List<Car> findByAvailableTrue();

    List<Car> findTop6ByAvailableTrueOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Car c WHERE c.id = :id")
    Optional<Car> findByIdWithLock(@Param("id") Long id);
}
