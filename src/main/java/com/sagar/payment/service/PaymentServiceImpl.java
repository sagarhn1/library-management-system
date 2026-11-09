package com.sagar.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.sagar.access.model.UserBookAccess;
import com.sagar.access.repository.UserBookAccessRepository;
import com.sagar.book.model.Book;
import com.sagar.book.model.BookAccessType;
import com.sagar.book.repository.BookRepository;
import com.sagar.exception.BadRequestException;
import com.sagar.exception.ResourceNotFoundException;
import com.sagar.payment.mapper.PaymentMapper;
import com.sagar.payment.model.Payment;
import com.sagar.payment.model.PaymentStatus;
import com.sagar.payment.payload.request.CreatePaymentRequest;
import com.sagar.payment.payload.request.PaymentFailedRequest;
import com.sagar.payment.payload.request.VerifyPaymentRequest;
import com.sagar.payment.payload.response.CreatePaymentResponse;
import com.sagar.payment.repository.PaymentRepository;
import com.sagar.user.model.User;
import com.sagar.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final RazorpayClient razorpayClient;
    private final PaymentMapper paymentMapper;
    private final UserBookAccessRepository userBookAccessRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;


    @Override
    public CreatePaymentResponse createPayment(
            CreatePaymentRequest request,
            String userEmail
    ) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Book book = bookRepository.findById(
                        request.getBookId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found"
                        )
                );

        // Only active books can be purchased.
        if (!Boolean.TRUE.equals(book.getActive())) {
            throw new BadRequestException(
                    "Book is currently inactive"
            );
        }

        if (book.getEbookFileKey() == null
                || book.getEbookFileKey().isBlank()) {

            throw new BadRequestException(
                    "E-book file is not available"
            );
        }

        // This payment endpoint is only for PAID books.
        if (book.getAccessType() != BookAccessType.PAID) {
            throw new BadRequestException(
                    "This book does not require payment"
            );
        }

        // Check whether the user already has active access.
        LocalDateTime now = LocalDateTime.now();

        boolean hasActiveAccess =
                userBookAccessRepository
                        .findByUserIdAndBookId(
                                user.getId(),
                                book.getId()
                        )
                        .map(access ->
                                !Boolean.TRUE.equals(
                                        access.getRevoked()
                                )
                                        && access.getAccessExpiresAt() != null
                                        && access.getAccessExpiresAt()
                                        .isAfter(now)
                        )
                        .orElse(false);

        if (hasActiveAccess) {
            throw new BadRequestException(
                    "You already have active access to this book"
            );
        }

        // Validate book price.
        if (book.getPrice() == null
                || book.getPrice()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BadRequestException(
                    "Invalid book price"
            );
        }

        try {

            // Razorpay expects amount in paise.
            long amountInPaise = book.getPrice()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            JSONObject orderRequest = new JSONObject();

            orderRequest.put(
                    "amount",
                    amountInPaise
            );

            orderRequest.put(
                    "currency",
                    "INR"
            );

            // Create Razorpay order.
            Order razorpayOrder =
                    razorpayClient.orders.create(
                            orderRequest
                    );

            // Save local payment record.
            Payment payment =
                    paymentMapper.toEntity(
                            user,
                            book,
                            razorpayOrder.get("id")
                    );

            Payment savedPayment =
                    paymentRepository.save(payment);

            return paymentMapper
                    .toCreatePaymentResponse(
                            savedPayment,
                            razorpayKeyId
                    );

        } catch (RazorpayException e) {

            throw new BadRequestException(
                    "Unable to create payment order"
            );
        }
    }


    @Override
    public void verifyPayment(
            VerifyPaymentRequest request,
            String userEmail
    ) {

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Payment payment = paymentRepository
                .findByRazorpayOrderId(
                        request.getRazorpayOrderId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment order not found"
                        )
                );

        // Security: user can verify only their own payment.
        if (!payment.getUser().getId()
                .equals(user.getId())) {

            throw new BadRequestException(
                    "You are not authorized to verify this payment"
            );
        }

        // Already verified successfully.
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        try {

            JSONObject attributes =
                    new JSONObject();

            attributes.put(
                    "razorpay_order_id",
                    payment.getRazorpayOrderId()
            );

            attributes.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId()
            );

            attributes.put(
                    "razorpay_signature",
                    request.getRazorpaySignature()
            );

            // Verify Razorpay signature.
            boolean isValid =
                    Utils.verifyPaymentSignature(
                            attributes,
                            razorpayKeySecret
                    );

            // Do not grant access if verification fails.
            if (!isValid) {
                throw new BadRequestException(
                        "Payment verification failed"
                );
            }

            // Payment successfully verified.
            payment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId()
            );

            payment.setStatus(
                    PaymentStatus.SUCCESS
            );

            paymentRepository.save(payment);


            // Grant book access.
            LocalDateTime now =
                    LocalDateTime.now();

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

            userBookAccessRepository.save(access);

        } catch (RazorpayException e) {

            throw new BadRequestException(
                    "Unable to verify payment signature"
            );
        }
    }


    @Override
    public void markPaymentAsFailed(
            PaymentFailedRequest request,
            String userEmail
    ) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Payment payment = paymentRepository
                .findByRazorpayOrderId(
                        request.getRazorpayOrderId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment order not found"
                        )
                );

        // Security: user can only update their own payment.
        if (!payment.getUser().getId()
                .equals(user.getId())) {

            throw new BadRequestException(
                    "You are not authorized to update this payment"
            );
        }

        // Never change a successful payment to FAILED.
        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            throw new BadRequestException(
                    "Successful payment cannot be marked as failed"
            );
        }

        // If already failed, do nothing.
        if (payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);

        paymentRepository.save(payment);
    }
}