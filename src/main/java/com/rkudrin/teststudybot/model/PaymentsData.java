package com.rkudrin.teststudybot.model;

import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments_data")
@NoArgsConstructor
public class PaymentsData {

    @Id
    private Long paymentId;

    private LocalDateTime studyPaidDate;

    private String currentPayment;

    public PaymentsData(Long paymentId, LocalDateTime studyPaidDate, String currentPayment) {
        this.paymentId = paymentId;
        this.studyPaidDate = studyPaidDate;
        this.currentPayment = currentPayment;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;
}
