package com.suvrat.movieofferservice.offer.dto;

import com.suvrat.movieofferservice.offer.model.OfferType;
import com.suvrat.movieofferservice.offer.model.OfferOrigin;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateOfferRequest(
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
}
