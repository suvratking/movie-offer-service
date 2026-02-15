package com.suvrat.movieofferservice.offer.dto;

import com.suvrat.movieofferservice.offer.model.Offer;
import com.suvrat.movieofferservice.offer.model.OfferOrigin;
import com.suvrat.movieofferservice.offer.model.OfferType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OfferResponse(
        Long id,
        String code,
        String title,
        String description,
        OfferType offerType,
        BigDecimal discountValue,
        BigDecimal maxDiscount,
        BigDecimal minOrderAmount,
        LocalDateTime validFrom,
        LocalDateTime validTill,
        Integer totalUsageLimit,
        Integer perUserUsageLimit,
        Boolean active,
        String applicableMovieId,
        String applicableTheaterId,
        String paymentPartner,
        String assignedUserId,
        OfferOrigin offerOrigin,
        String sourceApp,
        String sourceOfferCode
) {
    public static OfferResponse from(Offer offer) {
        return new OfferResponse(
                offer.getId(),
                offer.getCode(),
                offer.getTitle(),
                offer.getDescription(),
                offer.getOfferType(),
                offer.getDiscountValue(),
                offer.getMaxDiscount(),
                offer.getMinOrderAmount(),
                offer.getValidFrom(),
                offer.getValidTill(),
                offer.getTotalUsageLimit(),
                offer.getPerUserUsageLimit(),
                offer.getActive(),
                offer.getApplicableMovieId(),
                offer.getApplicableTheaterId(),
                offer.getPaymentPartner(),
                offer.getAssignedUserId(),
                offer.getOfferOrigin(),
                offer.getSourceApp(),
                offer.getSourceOfferCode()
        );
    }
}
