package com.suvrat.movieofferservice.offer.dto;

import java.math.BigDecimal;

public record ApplyOfferResponse(
        String code,
        String bookingId,
        BigDecimal orderAmount,
        BigDecimal discountAmount,
        BigDecimal finalPayable
) {
}
