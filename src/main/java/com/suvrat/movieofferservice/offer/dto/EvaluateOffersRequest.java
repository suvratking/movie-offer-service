package com.suvrat.movieofferservice.offer.dto;

import java.math.BigDecimal;

public record EvaluateOffersRequest(
        String userId,
        String movieId,
        String theaterId,
        String paymentPartner,
        String sourceApp,
        BigDecimal orderAmount
) {
}
