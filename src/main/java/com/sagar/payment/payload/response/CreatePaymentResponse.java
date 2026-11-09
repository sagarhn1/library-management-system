package com.sagar.payment.payload.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentResponse {

    private Long paymentId;

    private String razorpayOrderId;

    private String razorpayKeyId;

    private BigDecimal amount;

    private String currency;

    private String bookTitle;
}