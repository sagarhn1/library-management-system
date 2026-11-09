package com.sagar.payment.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedRequest {

    @NotBlank(
            message = "Razorpay order ID is required"
    )
    private String razorpayOrderId;
}