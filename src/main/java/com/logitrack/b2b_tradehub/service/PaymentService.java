package com.logitrack.b2b_tradehub.service;

import com.logitrack.b2b_tradehub.dto.payment.PaymentRequest;
import com.logitrack.b2b_tradehub.dto.payment.PaymentResponse;
import com.logitrack.b2b_tradehub.entity.Order;
import com.logitrack.b2b_tradehub.entity.Payment;
import com.logitrack.b2b_tradehub.entity.enums.PaymentStatus;
import com.logitrack.b2b_tradehub.entity.enums.PaymentType;
import com.logitrack.b2b_tradehub.exception.BusinessValidationException;
import com.logitrack.b2b_tradehub.exception.ResourceNotFoundException;
import com.logitrack.b2b_tradehub.mapper.PaymentMapper;
import com.logitrack.b2b_tradehub.repository.OrderRepository;
import com.logitrack.b2b_tradehub.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final OrderService orderService; // To update order remaining amount

    // Requirement: EF 5 (Register Payment)
    @Transactional
    public PaymentResponse create(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Validate Limit for ESPECES (20,000 DH)
        if (request.getTypePaiement() == PaymentType.ESPECE && request.getMontant().doubleValue() > 20000) {
            throw new BusinessValidationException("Payment rejected: Cash limit is 20,000 DH.");
        }

        Payment payment = paymentMapper.toEntity(request);
        payment.setOrder(order);

        // Initial Status logic
        if (request.getTypePaiement() == PaymentType.ESPECE) {
            payment.setStatus(PaymentStatus.ENCAISSE); // Cash is immediate
            // Update order immediately
            orderService.updatePaymentAmount(order.getId(), payment.getMontant());
        } else {
            payment.setStatus(PaymentStatus.EN_ATTENTE); // Checks/Transfers wait
        }

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    // Requirement: EF 5 (Process Payment - Encaisser)
    @Transactional
    public PaymentResponse processPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.ENCAISSE) {
            throw new BusinessValidationException("Payment already processed.");
        }

        payment.setStatus(PaymentStatus.ENCAISSE);
        payment.setDateEncaissement(LocalDate.now());

        // Update Order remaining amount now that money is received
        orderService.updatePaymentAmount(payment.getOrder().getId(), payment.getMontant());

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse rejectPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.REJETE);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    // Helper to update due date (only if pending)
    @Transactional
    public void updateEcheance(Long id, LocalDate newDate) {
        Payment payment = paymentRepository.findById(id).orElseThrow();
        if(payment.getStatus() != PaymentStatus.EN_ATTENTE) throw new BusinessValidationException("Cannot update processed payment");
        payment.setDateEcheance(newDate);
        paymentRepository.save(payment);
    }

    // Read methods
    public List<PaymentResponse> findAll() {
        return paymentRepository.findAll().stream().map(paymentMapper::toResponse).collect(Collectors.toList());
    }
    public PaymentResponse findById(Long id) {
        return paymentMapper.toResponse(paymentRepository.findById(id).orElseThrow());
    }
    public List<PaymentResponse> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).stream().map(paymentMapper::toResponse).collect(Collectors.toList());
    }
    public List<PaymentResponse> findPendingPayments() {
        return paymentRepository.findByStatus(PaymentStatus.EN_ATTENTE).stream().map(paymentMapper::toResponse).collect(Collectors.toList());
    }
}