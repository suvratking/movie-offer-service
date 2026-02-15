package com.suvrat.movieofferservice.offer.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "offers")
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferType offerType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validTill;

    @Column
    private Integer totalUsageLimit;

    @Column(nullable = false)
    private Integer perUserUsageLimit;

    @Column(nullable = false)
    private Boolean active;

    @Column(length = 64)
    private String applicableMovieId;

    @Column(length = 64)
    private String applicableTheaterId;

    @Column(length = 32)
    private String paymentPartner;

    @Column(length = 64)
    private String assignedUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferOrigin offerOrigin;

    @Column(length = 64)
    private String sourceApp;

    @Column(length = 64, unique = true)
    private String sourceOfferCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
