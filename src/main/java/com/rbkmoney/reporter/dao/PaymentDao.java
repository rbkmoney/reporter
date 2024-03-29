package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentAdditionalInfo;
import com.rbkmoney.reporter.domain.tables.records.PaymentRecord;
import com.rbkmoney.reporter.model.PaymentFundsAmount;
import org.jooq.Cursor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PaymentDao {

    Long savePayment(Payment payment);

    List<Payment> getPaymentsByState(LocalDateTime dateFrom,
                                     LocalDateTime dateTo,
                                     List<InvoicePaymentStatus> statuses);

    void saveAdditionalPaymentInfo(PaymentAdditionalInfo paymentAdditionalInfo);

    PaymentFundsAmount getPaymentFundsAmount(String partyId,
                                             String shopId,
                                             String currencyCode,
                                             Optional<LocalDateTime> fromTime,
                                             LocalDateTime toTime);

    PaymentRecord getPayment(String partyId, String shopId, String invoiceId, String paymentId);

    Cursor<PaymentRecord> getPaymentsCursor(String partyId,
                                            String shopId,
                                            Optional<LocalDateTime> fromTime,
                                            LocalDateTime toTime);

}
