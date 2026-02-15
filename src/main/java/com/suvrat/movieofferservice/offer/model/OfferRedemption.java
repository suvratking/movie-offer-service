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
@Table(name = "offer_redemptions")
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Column(nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 64, unique = true)
    private String bookingId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal orderAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private LocalDateTime redeemedAt;

    @PrePersist
    public void onCreate() {
        this.redeemedAt = LocalDateTime.now();
    }
}
