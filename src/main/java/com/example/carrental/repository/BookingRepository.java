package com.example.carrental.repository;

import com.example.carrental.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(String customerEmail);

    List<Booking> findByCarIdOrderByStartDateDesc(Long carId);

    List<Booking> findAllByOrderByCreatedAtDesc();

    @Query("SELECT b FROM Booking b WHERE b.car.id = :carId " +
           "AND b.status <> 'CANCELLED' " +
           "AND b.startDate <= :endDate " +
           "AND b.endDate >= :startDate " +
           "ORDER BY b.startDate ASC")
    List<Booking> findOverlappingBookings(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT b FROM Booking b WHERE b.car.id = :carId " +
           "AND b.status <> 'CANCELLED' " +
           "AND b.endDate >= :currentDate " +
           "ORDER BY b.startDate ASC")
    List<Booking> findUpcomingActiveBookings(
            @Param("carId") Long carId,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * Pessimistic-write-locked overlap check for concurrent booking safety.
     * Used exclusively by BookingService.createBooking() to prevent double-booking.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.car.id = :carId " +
           "AND b.status <> 'CANCELLED' " +
           "AND b.startDate <= :endDate " +
           "AND b.endDate >= :startDate")
    List<Booking> findOverlappingBookingsWithLock(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

