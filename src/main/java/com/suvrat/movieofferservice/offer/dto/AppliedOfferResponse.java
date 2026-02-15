package com.suvrat.movieofferservice.offer.dto;

import com.suvrat.movieofferservice.offer.model.OfferRedemption;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public record AppliedOfferResponse(
        String offerCode,
        String bookingId,
        String userId,
        BigDecimal orderAmount,
        BigDecimal discountAmount,
        BigDecimal finalPayable,
        LocalDateTime redeemedAt
) {
    public static AppliedOfferResponse from(OfferRedemption redemption) {
        BigDecimal finalPayable = redemption.getOrderAmount().subtract(redemption.getDiscountAmount());
        return new AppliedOfferResponse(
                redemption.getOffer().getCode(),
                redemption.getBookingId(),
                redemption.getUserId(),
                redemption.getOrderAmount().setScale(2, RoundingMode.HALF_UP),
                redemption.getDiscountAmount().setScale(2, RoundingMode.HALF_UP),
                finalPayable.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                redemption.getRedeemedAt()
        );
    }
}
