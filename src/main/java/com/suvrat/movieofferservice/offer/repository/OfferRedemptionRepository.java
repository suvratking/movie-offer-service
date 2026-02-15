package com.suvrat.movieofferservice.offer.repository;

import com.suvrat.movieofferservice.offer.model.Offer;
import com.suvrat.movieofferservice.offer.model.OfferRedemption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OfferRedemptionRepository extends JpaRepository<OfferRedemption, Long> {

    long countByOffer(Offer offer);

    long countByOfferAndUserId(Offer offer, String userId);

    boolean existsByBookingId(String bookingId);

    @Query("select r from OfferRedemption r join fetch r.offer order by r.redeemedAt desc")
    List<OfferRedemption> findAllWithOfferByOrderByRedeemedAtDesc();
}
