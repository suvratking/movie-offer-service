package com.suvrat.movieofferservice.offer.service;

import com.suvrat.movieofferservice.offer.dto.ApplyOfferRequest;
import com.suvrat.movieofferservice.offer.dto.ApplyOfferResponse;
import com.suvrat.movieofferservice.offer.dto.AppliedOfferResponse;
import com.suvrat.movieofferservice.offer.dto.CreateOfferRequest;
import com.suvrat.movieofferservice.offer.exception.OfferValidationException;
import com.suvrat.movieofferservice.offer.model.Offer;
import com.suvrat.movieofferservice.offer.model.OfferOrigin;
import com.suvrat.movieofferservice.offer.model.OfferRedemption;
import com.suvrat.movieofferservice.offer.model.OfferType;
import com.suvrat.movieofferservice.offer.repository.OfferRedemptionRepository;
import com.suvrat.movieofferservice.offer.repository.OfferRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private OfferRedemptionRepository offerRedemptionRepository;

    @InjectMocks
    private OfferService offerService;

    private CreateOfferRequest createOfferRequest;
    private Offer activeOffer;

    @BeforeEach
    void setUp() {
        createOfferRequest = new CreateOfferRequest(
                "test10",
                "Test Offer",
                "Offer for unit tests",
                OfferType.PERCENTAGE,
                new BigDecimal("10"),
                new BigDecimal("100"),
                new BigDecimal("100"),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                100,
                2,
                true,
                null,
                null,
                null,
                null,
                OfferOrigin.INTERNAL,
                null,
                null
        );

        activeOffer = Offer.builder()
                .id(1L)
                .code("TEST10")
                .title("Test Offer")
                .description("Offer for unit tests")
                .offerType(OfferType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .maxDiscount(new BigDecimal("100"))
                .minOrderAmount(new BigDecimal("100"))
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTill(LocalDateTime.now().plusDays(10))
                .totalUsageLimit(100)
                .perUserUsageLimit(2)
                .active(true)
                .offerOrigin(OfferOrigin.INTERNAL)
                .build();
    }

    @Test
    void createOffer_shouldNormalizeCodeAndPersist() {
        when(offerRepository.findOfferByCode("TEST10")).thenReturn(Optional.empty());
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        offerService.createOffer(createOfferRequest);

        ArgumentCaptor<Offer> offerCaptor = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository).save(offerCaptor.capture());
        assertEquals("TEST10", offerCaptor.getValue().getCode());
    }

    @Test
    void applyOffer_shouldUseLockedLookupAndReturnCalculatedAmounts() {
        when(offerRedemptionRepository.existsByBookingId("booking-1")).thenReturn(false);
        when(offerRepository.findByCode("TEST10")).thenReturn(Optional.of(activeOffer));
        when(offerRedemptionRepository.countByOffer(activeOffer)).thenReturn(0L);
        when(offerRedemptionRepository.countByOfferAndUserId(activeOffer, "user-1")).thenReturn(0L);
        when(offerRedemptionRepository.save(any(OfferRedemption.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplyOfferResponse response = offerService.applyOffer(new ApplyOfferRequest(
                "test10",
                "user-1",
                "booking-1",
                "movie-1",
                "theater-1",
                "CARD",
                null,
                new BigDecimal("500")
        ));

        assertEquals("TEST10", response.code());
        assertEquals(new BigDecimal("50.00"), response.discountAmount());
        assertEquals(new BigDecimal("450.00"), response.finalPayable());
    }

    @Test
    void applyOffer_shouldFailWhenBookingAlreadyHasOffer() {
        when(offerRedemptionRepository.existsByBookingId("booking-1")).thenReturn(true);

        assertThrows(OfferValidationException.class, () -> offerService.applyOffer(new ApplyOfferRequest(
                "TEST10",
                "user-1",
                "booking-1",
                "movie-1",
                "theater-1",
                "CARD",
                null,
                new BigDecimal("500")
        )));

        verify(offerRepository, never()).findByCode(any());
    }

    @Test
    void listAppliedOffers_shouldReturnMappedResponse() {
        OfferRedemption redemption = OfferRedemption.builder()
                .offer(activeOffer)
                .bookingId("booking-9")
                .userId("user-9")
                .orderAmount(new BigDecimal("400"))
                .discountAmount(new BigDecimal("40"))
                .redeemedAt(LocalDateTime.now())
                .build();
        when(offerRedemptionRepository.findAllByOrderByRedeemedAtDesc()).thenReturn(List.of(redemption));

        List<AppliedOfferResponse> responses = offerService.listAppliedOffers();

        assertEquals(1, responses.size());
        assertEquals("TEST10", responses.getFirst().offerCode());
        assertEquals(new BigDecimal("360.00"), responses.getFirst().finalPayable());
    }
}
