package com.logitrack.b2b_tradehub.repository;

import com.logitrack.b2b_tradehub.entity.Payment;
import com.logitrack.b2b_tradehub.entity.enums.PaymentType;
import com.logitrack.b2b_tradehub.entity.enums.PaymentStatus;
import com.logitrack.b2b_tradehub.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    

    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByTypePaiement(PaymentType type);
    Optional<Payment> findByNumeroPaiement(String numeroPaiement);
    List<Payment> findByDatePaiementBetween(LocalDate startDate, LocalDate endDate);

}
