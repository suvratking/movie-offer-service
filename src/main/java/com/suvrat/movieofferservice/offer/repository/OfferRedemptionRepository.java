package com.suvrat.movieofferservice.offer.repository;

import com.suvrat.movieofferservice.offer.model.Offer;
import com.suvrat.movieofferservice.offer.model.OfferRedemption;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRedemptionRepository extends JpaRepository<OfferRedemption, Long> {

    long countByOffer(Offer offer);

    long countByOfferAndUserId(Offer offer, String userId);

    boolean existsByBookingId(String bookingId);

    @EntityGraph(attributePaths = "offer")
    List<OfferRedemption> findAllByOrderByRedeemedAtDesc();
}
