package com.suvrat.movieofferservice.offer.repository;

import com.suvrat.movieofferservice.offer.model.Offer;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Optional<Offer> findByCode(String code);
    Optional<Offer> findBySourceOfferCode(String sourceOfferCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Offer o where o.code = :code")
    Optional<Offer> findByCodeForUpdate(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Offer o where o.sourceOfferCode = :sourceOfferCode")
    Optional<Offer> findBySourceOfferCodeForUpdate(String sourceOfferCode);

    List<Offer> findByActiveTrue();
}
