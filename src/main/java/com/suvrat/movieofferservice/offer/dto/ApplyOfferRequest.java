package com.suvrat.movieofferservice.offer.dto;

import java.math.BigDecimal;

public record ApplyOfferRequest(
        String code,
        String userId,
        String bookingId,
        String movieId,
        String theaterId,
        String paymentPartner,
        String sourceApp,
        BigDecimal orderAmount
) {
}
