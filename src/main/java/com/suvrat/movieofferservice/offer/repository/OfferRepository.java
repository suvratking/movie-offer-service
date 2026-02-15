package com.suvrat.movieofferservice.offer.repository;

import com.suvrat.movieofferservice.offer.model.Offer;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Optional<Offer> findOfferByCode(String code);
    Optional<Offer> findOfferBySourceOfferCode(String sourceOfferCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Offer> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Offer> findBySourceOfferCode(String sourceOfferCode);

    List<Offer> findByActiveTrue();
}
