package com.example.carrental.repository;

import com.example.carrental.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    Optional<Partner> findByPartnerCodeIgnoreCase(String partnerCode);
    boolean existsByPartnerCodeIgnoreCase(String partnerCode);
}
