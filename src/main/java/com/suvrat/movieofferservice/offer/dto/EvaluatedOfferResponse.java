package com.suvrat.movieofferservice.offer.dto;

import java.math.BigDecimal;

public record EvaluatedOfferResponse(
        String code,
        String title,
        BigDecimal estimatedDiscount,
        BigDecimal finalPayable,
        String reason
) {
}
