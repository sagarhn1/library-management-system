package com.sagar.payment.mapper;

import com.sagar.book.model.Book;
import com.sagar.payment.model.Payment;
import com.sagar.payment.model.PaymentStatus;
import com.sagar.payment.payload.response.CreatePaymentResponse;
import com.sagar.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(
            User user,
            Book book,
            String razorpayOrderId
    ) {

        return Payment.builder()
                .user(user)
                .book(book)
                .amount(book.getPrice())
                .status(PaymentStatus.CREATED)
                .razorpayOrderId(razorpayOrderId)
                .build();
    }

    public CreatePaymentResponse toCreatePaymentResponse(
            Payment payment,
            String razorpayKeyId
    ) {

        return CreatePaymentResponse.builder()
                .paymentId(payment.getId())
                .razorpayOrderId(
                        payment.getRazorpayOrderId()
                )
                .razorpayKeyId(razorpayKeyId)
                .amount(payment.getAmount())
                .currency("INR")
                .bookTitle(payment.getBook().getTitle())
                .build();
    }
}