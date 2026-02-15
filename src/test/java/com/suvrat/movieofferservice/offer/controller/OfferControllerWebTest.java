package com.suvrat.movieofferservice.offer.controller;

import com.suvrat.movieofferservice.offer.dto.ApplyOfferRequest;
import com.suvrat.movieofferservice.offer.dto.ApplyOfferResponse;
import com.suvrat.movieofferservice.offer.dto.AppliedOfferResponse;
import com.suvrat.movieofferservice.offer.dto.CreateOfferRequest;
import com.suvrat.movieofferservice.offer.dto.EvaluateOffersRequest;
import com.suvrat.movieofferservice.offer.dto.EvaluatedOfferResponse;
import com.suvrat.movieofferservice.offer.dto.OfferResponse;
import com.suvrat.movieofferservice.offer.exception.ApiExceptionHandler;
import com.suvrat.movieofferservice.offer.exception.OfferValidationException;
import com.suvrat.movieofferservice.offer.model.OfferOrigin;
import com.suvrat.movieofferservice.offer.model.OfferType;
import com.suvrat.movieofferservice.offer.service.OfferService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OfferController.class)
@Import(ApiExceptionHandler.class)
class OfferControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OfferService offerService;

    @Test
    void createOffer_shouldReturnCreated() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        OfferResponse response = new OfferResponse(
                1L,
                "TEST10",
                "Test Offer",
                "desc",
                OfferType.PERCENTAGE,
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                new BigDecimal("100.00"),
                now.minusDays(1),
                now.plusDays(1),
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
        when(offerService.createOffer(any(CreateOfferRequest.class))).thenReturn(response);

        String payload = """
                {
                  "code":"TEST10",
                  "title":"Test Offer",
                  "description":"desc",
                  "offerType":"PERCENTAGE",
                  "discountValue":10,
                  "maxDiscount":50,
                  "minOrderAmount":100,
                  "validFrom":"2026-02-14T10:00:00",
                  "validTill":"2026-02-20T10:00:00",
                  "totalUsageLimit":100,
                  "perUserUsageLimit":2,
                  "active":true,
                  "offerOrigin":"INTERNAL"
                }
                """;

        mockMvc.perform(post("/api/v1/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("TEST10"));
    }

    @Test
    void listOffers_shouldReturnOk() throws Exception {
        when(offerService.listOffers()).thenReturn(List.of(
                new OfferResponse(
                        1L,
                        "WELCOME20",
                        "Welcome",
                        "desc",
                        OfferType.PERCENTAGE,
                        new BigDecimal("20.00"),
                        new BigDecimal("200.00"),
                        new BigDecimal("100.00"),
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1),
                        100,
                        1,
                        true,
                        null,
                        null,
                        null,
                        null,
                        OfferOrigin.INTERNAL,
                        null,
                        null
                )
        ));

        mockMvc.perform(get("/api/v1/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("WELCOME20"));
    }

    @Test
    void evaluateOffers_shouldReturnOk() throws Exception {
        when(offerService.evaluateOffers(any(EvaluateOffersRequest.class))).thenReturn(List.of(
                new EvaluatedOfferResponse(
                        "WELCOME20",
                        "Welcome",
                        new BigDecimal("100.00"),
                        new BigDecimal("400.00"),
                        "ELIGIBLE"
                )
        ));

        String payload = """
                {
                  "userId":"user-1",
                  "movieId":"movie-1",
                  "theaterId":"theater-1",
                  "paymentPartner":"CARD",
                  "sourceApp":null,
                  "orderAmount":500
                }
                """;

        mockMvc.perform(post("/api/v1/offers/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("WELCOME20"));
    }

    @Test
    void applyOffer_shouldReturnBadRequestForValidationError() throws Exception {
        when(offerService.applyOffer(any(ApplyOfferRequest.class)))
                .thenThrow(new OfferValidationException("Offer is not eligible"));

        String payload = """
                {
                  "code":"WELCOME20",
                  "userId":"user-1",
                  "bookingId":"booking-1",
                  "movieId":"movie-1",
                  "theaterId":"theater-1",
                  "paymentPartner":"CARD",
                  "sourceApp":null,
                  "orderAmount":500
                }
                """;

        mockMvc.perform(post("/api/v1/offers/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Offer is not eligible"));
    }

    @Test
    void listAppliedOffers_shouldReturnOk() throws Exception {
        when(offerService.listAppliedOffers()).thenReturn(List.of(
                new AppliedOfferResponse(
                        "WELCOME20",
                        "booking-1",
                        "user-1",
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("400.00"),
                        LocalDateTime.now()
                )
        ));

        mockMvc.perform(get("/api/v1/offers/applied"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].offerCode").value("WELCOME20"));
    }

    @Test
    void applyOffer_shouldReturnOk() throws Exception {
        when(offerService.applyOffer(any(ApplyOfferRequest.class))).thenReturn(
                new ApplyOfferResponse(
                        "WELCOME20",
                        "booking-1",
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("400.00")
                )
        );

        String payload = """
                {
                  "code":"WELCOME20",
                  "userId":"user-1",
                  "bookingId":"booking-1",
                  "movieId":"movie-1",
                  "theaterId":"theater-1",
                  "paymentPartner":"CARD",
                  "sourceApp":null,
                  "orderAmount":500
                }
                """;

        mockMvc.perform(post("/api/v1/offers/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("WELCOME20"))
                .andExpect(jsonPath("$.finalPayable").value(400.00));
    }
}
