package com.suvrat.movieofferservice.offer.controller;

import com.suvrat.movieofferservice.offer.dto.ApplyOfferRequest;
import com.suvrat.movieofferservice.offer.dto.ApplyOfferResponse;
import com.suvrat.movieofferservice.offer.dto.AppliedOfferResponse;
import com.suvrat.movieofferservice.offer.dto.CreateOfferRequest;
import com.suvrat.movieofferservice.offer.dto.EvaluateOffersRequest;
import com.suvrat.movieofferservice.offer.dto.EvaluatedOfferResponse;
import com.suvrat.movieofferservice.offer.dto.OfferResponse;
import com.suvrat.movieofferservice.offer.service.OfferService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferResponse createOffer(@RequestBody CreateOfferRequest request) {
        return offerService.createOffer(request);
    }

    @GetMapping
    public List<OfferResponse> listOffers() {
        return offerService.listOffers();
    }

    @PostMapping("/evaluate")
    public List<EvaluatedOfferResponse> evaluateOffers(@RequestBody EvaluateOffersRequest request) {
        return offerService.evaluateOffers(request);
    }

    @PostMapping("/apply")
    public ApplyOfferResponse applyOffer(@RequestBody ApplyOfferRequest request) {
        return offerService.applyOffer(request);
    }

    @GetMapping("/applied")
    public List<AppliedOfferResponse> listAppliedOffers() {
        return offerService.listAppliedOffers();
    }
}
