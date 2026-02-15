package com.suvrat.movieofferservice.offer.service;

import com.suvrat.movieofferservice.offer.dto.ApplyOfferRequest;
import com.suvrat.movieofferservice.offer.dto.ApplyOfferResponse;
import com.suvrat.movieofferservice.offer.dto.AppliedOfferResponse;
import com.suvrat.movieofferservice.offer.dto.CreateOfferRequest;
import com.suvrat.movieofferservice.offer.dto.EvaluateOffersRequest;
import com.suvrat.movieofferservice.offer.dto.EvaluatedOfferResponse;
import com.suvrat.movieofferservice.offer.dto.OfferResponse;
import com.suvrat.movieofferservice.offer.exception.OfferValidationException;
import com.suvrat.movieofferservice.offer.model.Offer;
import com.suvrat.movieofferservice.offer.model.OfferOrigin;
import com.suvrat.movieofferservice.offer.model.OfferRedemption;
import com.suvrat.movieofferservice.offer.model.OfferType;
import com.suvrat.movieofferservice.offer.repository.OfferRedemptionRepository;
import com.suvrat.movieofferservice.offer.repository.OfferRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final OfferRedemptionRepository offerRedemptionRepository;

    public OfferService(OfferRepository offerRepository, OfferRedemptionRepository offerRedemptionRepository) {
        this.offerRepository = offerRepository;
        this.offerRedemptionRepository = offerRedemptionRepository;
    }

    public OfferResponse createOffer(CreateOfferRequest request) {
        validateCreateRequest(request);
        String normalizedCode = normalizeOfferCode(request.code());
        offerRepository.findByCode(normalizedCode).ifPresent(existing -> {
            throw new OfferValidationException("Offer code already exists");
        });
        String normalizedSourceOfferCode = normalizeOfferCode(request.sourceOfferCode());
        if (normalizedSourceOfferCode != null) {
            offerRepository.findBySourceOfferCode(normalizedSourceOfferCode).ifPresent(existing -> {
                throw new OfferValidationException("Source offer code already exists");
            });
        }

        Offer offer = Offer.builder()
                .code(normalizedCode)
                .title(request.title().trim())
                .description(request.description())
                .offerType(request.offerType())
                .discountValue(request.discountValue())
                .maxDiscount(request.maxDiscount())
                .minOrderAmount(request.minOrderAmount())
                .validFrom(request.validFrom())
                .validTill(request.validTill())
                .totalUsageLimit(request.totalUsageLimit())
                .perUserUsageLimit(request.perUserUsageLimit())
                .active(Boolean.TRUE.equals(request.active()))
                .applicableMovieId(normalizeNullable(request.applicableMovieId()))
                .applicableTheaterId(normalizeNullable(request.applicableTheaterId()))
                .paymentPartner(normalizeNullable(request.paymentPartner()))
                .assignedUserId(normalizeNullable(request.assignedUserId()))
                .offerOrigin(request.offerOrigin())
                .sourceApp(normalizeNullable(request.sourceApp()))
                .sourceOfferCode(normalizedSourceOfferCode)
                .build();

        return OfferResponse.from(offerRepository.save(offer));
    }

    public List<OfferResponse> listOffers() {
        return offerRepository.findAll().stream().map(OfferResponse::from).toList();
    }

    public List<AppliedOfferResponse> listAppliedOffers() {
        return offerRedemptionRepository.findAllWithOfferByOrderByRedeemedAtDesc().stream()
                .map(AppliedOfferResponse::from)
                .toList();
    }

    public List<EvaluatedOfferResponse> evaluateOffers(EvaluateOffersRequest request) {
        validateEvaluateRequest(request);
        return offerRepository.findByActiveTrue().stream()
                .map(offer -> evaluateSingleOffer(offer, request.userId(), request.movieId(), request.theaterId(),
                        request.paymentPartner(), request.sourceApp(), request.orderAmount()))
                .filter(result -> result != null)
                .sorted(Comparator.comparing(EvaluatedOfferResponse::estimatedDiscount).reversed())
                .toList();
    }

    @Transactional
    public ApplyOfferResponse applyOffer(ApplyOfferRequest request) {
        validateApplyRequest(request);

        if (offerRedemptionRepository.existsByBookingId(request.bookingId())) {
            throw new OfferValidationException("Offer already applied for this booking");
        }

        Offer offer = findOfferForApply(request.code());

        EvaluatedOfferResponse evaluated = evaluateSingleOffer(
                offer,
                request.userId(),
                request.movieId(),
                request.theaterId(),
                request.paymentPartner(),
                request.sourceApp(),
                request.orderAmount());

        if (evaluated == null) {
            throw new OfferValidationException("Offer is not eligible for this booking");
        }

        OfferRedemption redemption = OfferRedemption.builder()
                .offer(offer)
                .bookingId(request.bookingId().trim())
                .userId(request.userId().trim())
                .orderAmount(request.orderAmount())
                .discountAmount(evaluated.estimatedDiscount())
                .build();
        offerRedemptionRepository.save(redemption);

        return new ApplyOfferResponse(
                offer.getCode(),
                redemption.getBookingId(),
                request.orderAmount().setScale(2, RoundingMode.HALF_UP),
                evaluated.estimatedDiscount().setScale(2, RoundingMode.HALF_UP),
                evaluated.finalPayable().setScale(2, RoundingMode.HALF_UP)
        );
    }

    private void validateCreateRequest(CreateOfferRequest request) {
        if (request == null) {
            throw new OfferValidationException("Request body is required");
        }
        if (isBlank(request.code()) || isBlank(request.title()) || request.offerType() == null ||
                request.discountValue() == null || request.minOrderAmount() == null ||
                request.validFrom() == null || request.validTill() == null ||
                request.perUserUsageLimit() == null || request.offerOrigin() == null) {
            throw new OfferValidationException("Missing required fields");
        }
        if (request.discountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OfferValidationException("discountValue must be greater than 0");
        }
        if (request.minOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new OfferValidationException("minOrderAmount cannot be negative");
        }
        if (request.validTill().isBefore(request.validFrom())) {
            throw new OfferValidationException("validTill must be after validFrom");
        }
        if (request.totalUsageLimit() != null && request.totalUsageLimit() <= 0) {
            throw new OfferValidationException("totalUsageLimit must be greater than 0");
        }
        if (request.perUserUsageLimit() <= 0) {
            throw new OfferValidationException("perUserUsageLimit must be greater than 0");
        }
        if (request.offerType() == OfferType.PERCENTAGE && request.discountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new OfferValidationException("Percentage discount cannot be greater than 100");
        }
        if (request.offerOrigin() == OfferOrigin.THIRD_PARTY) {
            if (isBlank(request.sourceApp()) || isBlank(request.sourceOfferCode())) {
                throw new OfferValidationException("sourceApp and sourceOfferCode are required for THIRD_PARTY offers");
            }
        } else if (!isBlank(request.sourceOfferCode()) && isBlank(request.sourceApp())) {
            throw new OfferValidationException("sourceApp is required when sourceOfferCode is provided");
        }
    }

    private void validateEvaluateRequest(EvaluateOffersRequest request) {
        if (request == null || isBlank(request.userId()) || request.orderAmount() == null) {
            throw new OfferValidationException("userId and orderAmount are required");
        }
    }

    private void validateApplyRequest(ApplyOfferRequest request) {
        if (request == null || isBlank(request.code()) || isBlank(request.userId()) || isBlank(request.bookingId()) ||
                request.orderAmount() == null) {
            throw new OfferValidationException("code, userId, bookingId and orderAmount are required");
        }
    }

    private EvaluatedOfferResponse evaluateSingleOffer(
            Offer offer,
            String userId,
            String movieId,
            String theaterId,
            String paymentPartner,
            String sourceApp,
            BigDecimal orderAmount
    ) {
        if (!Boolean.TRUE.equals(offer.getActive())) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(offer.getValidFrom()) || now.isAfter(offer.getValidTill())) {
            return null;
        }

        if (orderAmount.compareTo(offer.getMinOrderAmount()) < 0) {
            return null;
        }

        if (!isApplicable(offer.getApplicableMovieId(), movieId) || !isApplicable(offer.getApplicableTheaterId(), theaterId)
                || !isApplicable(offer.getPaymentPartner(), paymentPartner)) {
            return null;
        }
        if (!isApplicable(offer.getAssignedUserId(), userId)) {
            return null;
        }
        if (offer.getOfferOrigin() == OfferOrigin.THIRD_PARTY && !isApplicable(offer.getSourceApp(), sourceApp)) {
            return null;
        }

        long totalUsage = offerRedemptionRepository.countByOffer(offer);
        if (offer.getTotalUsageLimit() != null && totalUsage >= offer.getTotalUsageLimit()) {
            return null;
        }

        long userUsage = offerRedemptionRepository.countByOfferAndUserId(offer, userId.trim());
        if (userUsage >= offer.getPerUserUsageLimit()) {
            return null;
        }

        BigDecimal discount = calculateDiscount(offer, orderAmount);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal finalPayable = orderAmount.subtract(discount);
        return new EvaluatedOfferResponse(
                offer.getCode(),
                offer.getTitle(),
                discount.setScale(2, RoundingMode.HALF_UP),
                finalPayable.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                "ELIGIBLE"
        );
    }

    private BigDecimal calculateDiscount(Offer offer, BigDecimal orderAmount) {
        BigDecimal discount;
        if (offer.getOfferType() == OfferType.PERCENTAGE) {
            discount = orderAmount.multiply(offer.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discount = offer.getDiscountValue();
        }

        if (offer.getMaxDiscount() != null && discount.compareTo(offer.getMaxDiscount()) > 0) {
            discount = offer.getMaxDiscount();
        }
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        return discount;
    }

    private boolean isApplicable(String ruleValue, String requestValue) {
        if (isBlank(ruleValue)) {
            return true;
        }
        if (isBlank(requestValue)) {
            return false;
        }
        return ruleValue.trim().equalsIgnoreCase(requestValue.trim());
    }

    private String normalizeNullable(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeOfferCode(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private Offer findOfferForApply(String requestedCode) {
        String normalizedCode = normalizeOfferCode(requestedCode);
        if (normalizedCode == null) {
            throw new OfferValidationException("code is required");
        }
        return offerRepository.findByCodeForUpdate(normalizedCode)
                .or(() -> offerRepository.findBySourceOfferCodeForUpdate(normalizedCode))
                .orElseThrow(() -> new OfferValidationException("Offer not found"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
