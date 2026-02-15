package com.suvrat.movieofferservice.offer.controller;

import com.suvrat.movieofferservice.offer.dto.ApplyOfferRequest;
import com.suvrat.movieofferservice.offer.dto.ApplyOfferResponse;
import com.suvrat.movieofferservice.offer.dto.AppliedOfferResponse;
import com.suvrat.movieofferservice.offer.dto.CreateOfferRequest;
import com.suvrat.movieofferservice.offer.dto.EvaluateOffersRequest;
import com.suvrat.movieofferservice.offer.dto.EvaluatedOfferResponse;
import com.suvrat.movieofferservice.offer.dto.OfferResponse;
import com.suvrat.movieofferservice.offer.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
@Tag(name = "Offers", description = "Create, evaluate, apply, and inspect movie booking offers")
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create an offer",
            description = "Creates a new movie booking offer and returns the created offer details."
    )
    public ResponseEntity<OfferResponse> createOffer(@RequestBody CreateOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.createOffer(request));
    }

    @GetMapping
    @Operation(
            summary = "List all offers",
            description = "Returns all available offers."
    )
    public ResponseEntity<List<OfferResponse>> listOffers() {
        return ResponseEntity.ok(offerService.listOffers());
    }

    @PostMapping("/evaluate")
    @Operation(
            summary = "Evaluate offers",
            description = "Evaluates eligible offers for the provided movie booking request."
    )
    public ResponseEntity<List<EvaluatedOfferResponse>> evaluateOffers(@RequestBody EvaluateOffersRequest request) {
        return ResponseEntity.ok(offerService.evaluateOffers(request));
    }

    @PostMapping("/apply")
    @Operation(
            summary = "Apply an offer",
            description = "Applies a selected offer to a booking and records redemption."
    )
    public ResponseEntity<ApplyOfferResponse> applyOffer(@RequestBody ApplyOfferRequest request) {
        return ResponseEntity.ok(offerService.applyOffer(request));
    }

    @GetMapping("/applied")
    @Operation(
            summary = "List applied offers",
            description = "Returns all applied offers and redemption history."
    )
    public ResponseEntity<List<AppliedOfferResponse>> listAppliedOffers() {
        return ResponseEntity.ok(offerService.listAppliedOffers());
    }
}
