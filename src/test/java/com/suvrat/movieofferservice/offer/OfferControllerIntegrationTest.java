package com.suvrat.movieofferservice.offer;

import com.suvrat.movieofferservice.offer.dto.ApplyOfferRequest;
import com.suvrat.movieofferservice.offer.dto.ApplyOfferResponse;
import com.suvrat.movieofferservice.offer.dto.AppliedOfferResponse;
import com.suvrat.movieofferservice.offer.dto.CreateOfferRequest;
import com.suvrat.movieofferservice.offer.dto.EvaluateOffersRequest;
import com.suvrat.movieofferservice.offer.dto.EvaluatedOfferResponse;
import com.suvrat.movieofferservice.offer.dto.OfferResponse;
import com.suvrat.movieofferservice.offer.exception.OfferValidationException;
import com.suvrat.movieofferservice.offer.model.OfferOrigin;
import com.suvrat.movieofferservice.offer.model.OfferType;
import com.suvrat.movieofferservice.offer.repository.OfferRedemptionRepository;
import com.suvrat.movieofferservice.offer.repository.OfferRepository;
import com.suvrat.movieofferservice.offer.service.OfferService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OfferControllerIntegrationTest {

    @Autowired
    private OfferService offerService;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private OfferRedemptionRepository offerRedemptionRepository;

    @BeforeEach
    void cleanup() {
        offerRedemptionRepository.deleteAll();
        offerRepository.deleteAll();
    }

    @Test
    void shouldCreateAndListOffers() {
        OfferResponse created = offerService.createOffer(createOfferRequest("WELCOME20", 2, null));
        List<OfferResponse> offers = offerService.listOffers();

        assertEquals("WELCOME20", created.code());
        assertTrue(offers.stream().anyMatch(offer -> "WELCOME20".equals(offer.code())));
    }

    @Test
    void shouldEvaluateAndApplyOffer() {
        offerService.createOffer(createOfferRequest("MOVIE30", 3, "PAYTM"));

        List<EvaluatedOfferResponse> evaluated = offerService.evaluateOffers(new EvaluateOffersRequest(
                "user-101", "movie-1", "theater-1", "PAYTM", null, new BigDecimal("400")
        ));

        assertFalse(evaluated.isEmpty());
        assertEquals("MOVIE30", evaluated.getFirst().code());
        assertEquals(new BigDecimal("120.00"), evaluated.getFirst().estimatedDiscount());

        ApplyOfferResponse applied = offerService.applyOffer(new ApplyOfferRequest(
                "MOVIE30", "user-101", "booking-101", "movie-1", "theater-1", "PAYTM", null, new BigDecimal("400")
        ));

        assertEquals(new BigDecimal("120.00"), applied.discountAmount());
        assertEquals(new BigDecimal("280.00"), applied.finalPayable());
    }

    @Test
    void shouldRejectApplyWhenPerUserLimitReached() {
        offerService.createOffer(createOfferRequest("LIMIT1", 1, null));

        offerService.applyOffer(new ApplyOfferRequest(
                "LIMIT1", "user-202", "booking-201", "movie-2", "theater-2", "CARD", null, new BigDecimal("500")
        ));

        assertThrows(OfferValidationException.class, () -> offerService.applyOffer(new ApplyOfferRequest(
                "LIMIT1", "user-202", "booking-202", "movie-2", "theater-2", "CARD", null, new BigDecimal("500")
        )));
    }

    @Test
    void shouldEvaluateUserSpecificOfferOnlyForAssignedUser() {
        offerService.createOffer(createOfferRequest("VIPUSER", 2, null, "user-vip", OfferOrigin.INTERNAL, null, null));

        List<EvaluatedOfferResponse> eligibleForVip = offerService.evaluateOffers(new EvaluateOffersRequest(
                "user-vip", "movie-1", "theater-1", "CARD", null, new BigDecimal("500")
        ));
        List<EvaluatedOfferResponse> notEligibleForOther = offerService.evaluateOffers(new EvaluateOffersRequest(
                "user-other", "movie-1", "theater-1", "CARD", null, new BigDecimal("500")
        ));

        assertFalse(eligibleForVip.isEmpty());
        assertEquals("VIPUSER", eligibleForVip.getFirst().code());
        assertTrue(notEligibleForOther.isEmpty());
    }

    @Test
    void shouldApplyOfferUsingThirdPartySourceOfferCode() {
        offerService.createOffer(createOfferRequest(
                "INT_BANK50", 5, "CARD", null, OfferOrigin.THIRD_PARTY, "PAYTM", "PAYTM50"
        ));

        List<EvaluatedOfferResponse> evaluated = offerService.evaluateOffers(new EvaluateOffersRequest(
                "user-300", "movie-3", "theater-3", "CARD", "PAYTM", new BigDecimal("400")
        ));
        assertFalse(evaluated.isEmpty());

        ApplyOfferResponse applied = offerService.applyOffer(new ApplyOfferRequest(
                "PAYTM50", "user-300", "booking-300", "movie-3", "theater-3", "CARD", "PAYTM", new BigDecimal("400")
        ));
        assertEquals("INT_BANK50", applied.code());
        assertEquals(new BigDecimal("120.00"), applied.discountAmount());
    }

    @Test
    void shouldListAppliedOffers() {
        offerService.createOffer(createOfferRequest("LIST100", 3, "CARD"));

        offerService.applyOffer(new ApplyOfferRequest(
                "LIST100", "user-777", "booking-777", "movie-7", "theater-7", "CARD", null, new BigDecimal("500")
        ));

        List<AppliedOfferResponse> appliedOffers = offerService.listAppliedOffers();

        assertFalse(appliedOffers.isEmpty());
        AppliedOfferResponse first = appliedOffers.getFirst();
        assertEquals("LIST100", first.offerCode());
        assertEquals("booking-777", first.bookingId());
        assertEquals("user-777", first.userId());
        assertEquals(new BigDecimal("500.00"), first.orderAmount());
    }

    private CreateOfferRequest createOfferRequest(
            String code,
            int perUserLimit,
            String paymentPartner,
            String assignedUserId,
            OfferOrigin offerOrigin,
            String sourceApp,
            String sourceOfferCode
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new CreateOfferRequest(
                code,
                code + " title",
                "Offer for tests",
                OfferType.PERCENTAGE,
                new BigDecimal("30"),
                new BigDecimal("150"),
                new BigDecimal("100"),
                now.minusDays(1),
                now.plusDays(2),
                100,
                perUserLimit,
                true,
                null,
                null,
                paymentPartner,
                assignedUserId,
                offerOrigin,
                sourceApp,
                sourceOfferCode
        );
    }

    private CreateOfferRequest createOfferRequest(String code, int perUserLimit, String paymentPartner) {
        return createOfferRequest(code, perUserLimit, paymentPartner, null, OfferOrigin.INTERNAL, null, null);
    }
}
