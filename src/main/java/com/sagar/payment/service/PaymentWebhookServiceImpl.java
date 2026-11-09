package com.sagar.payment.service;

import com.razorpay.Utils;
import com.sagar.access.model.UserBookAccess;
import com.sagar.access.repository.UserBookAccessRepository;
import com.sagar.payment.model.Payment;
import com.sagar.payment.model.PaymentStatus;
import com.sagar.payment.repository.PaymentRepository;
import com.sagar.user.model.User;
import com.sagar.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentWebhookServiceImpl
        implements PaymentWebhookService {

    private final PaymentRepository paymentRepository;
    private final UserBookAccessRepository userBookAccessRepository;

    @Override
    public void handleWebhook(
            String payload,
            String signature,
            String webhookSecret
    ) {

        try {

            // 1. Verify webhook signature
            boolean isValid =
                    Utils.verifyWebhookSignature(
                            payload,
                            signature,
                            webhookSecret
                    );

            if (!isValid) {
                throw new BadRequestException(
                        "Invalid Razorpay webhook signature"
                );
            }

            // 2. Parse webhook payload
            JSONObject json =
                    new JSONObject(payload);

            String event =
                    json.getString("event");

            JSONObject paymentEntity =
                    json.getJSONObject("payload")
                            .getJSONObject("payment")
                            .getJSONObject("entity");

            String razorpayPaymentId =
                    paymentEntity.getString("id");

            String razorpayOrderId =
                    paymentEntity.getString("order_id");

            // 3. Find our local payment
            Payment payment =
                    paymentRepository
                            .findByRazorpayOrderId(
                                    razorpayOrderId
                            )
                            .orElse(null);

            // Ignore orders not created by our system
            if (payment == null) {
                return;
            }

            // 4. Handle successful payment
            if ("payment.captured".equals(event)) {

                handlePaymentSuccess(
                        payment,
                        razorpayPaymentId
                );

            }

            // 5. Handle failed payment
            else if ("payment.failed".equals(event)) {

                if (payment.getStatus()
                        != PaymentStatus.SUCCESS) {

                    payment.setStatus(
                            PaymentStatus.FAILED
                    );

                    payment.setRazorpayPaymentId(
                            razorpayPaymentId
                    );

                    paymentRepository.save(payment);
                }
            }

        } catch (Exception e) {

            throw new BadRequestException(
                    "Unable to process Razorpay webhook"
            );
        }
    }


    private void handlePaymentSuccess(
            Payment payment,
            String razorpayPaymentId
    ) {

        // Idempotency:
        // Razorpay may send the same webhook again.
        if (payment.getStatus()
                == PaymentStatus.SUCCESS) {
            return;
        }

        payment.setRazorpayPaymentId(
                razorpayPaymentId
        );

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        paymentRepository.save(payment);


        // Grant book access
        LocalDateTime now =
                LocalDateTime.now();

        User user = payment.getUser();

        UserBookAccess access =
                userBookAccessRepository
                        .findByUserIdAndBookId(
                                user.getId(),
                                payment.getBook().getId()
                        )
                        .orElse(
                                UserBookAccess.builder()
                                        .user(user)
                                        .book(
                                                payment.getBook()
                                        )
                                        .build()
                        );

        access.setAccessGrantedAt(now);

        access.setAccessExpiresAt(
                now.plusDays(
                        payment.getBook()
                                .getAccessDurationDays()
                )
        );

        access.setRevoked(false);

        userBookAccessRepository.save(
                access
        );
    }
}