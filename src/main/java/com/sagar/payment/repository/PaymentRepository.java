package com.sagar.payment.repository;

import com.sagar.payment.model.Payment;
import com.sagar.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(
            String razorpayOrderId
    );

    Optional<Payment> findByRazorpayPaymentId(
            String razorpayPaymentId
    );

    boolean existsByUserIdAndBookIdAndStatus(
            Long userId,
            Long bookId,
            PaymentStatus status
    );
}