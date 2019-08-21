package com.rbkmoney.reporter.config;

import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.handle.InvoiceBatchHandler;
import com.rbkmoney.reporter.handle.impl.InvoiceBatchHandlerImpl;
import com.rbkmoney.reporter.mapper.impl.AdjustmentInvoiceBatchMapperImpl;
import com.rbkmoney.reporter.mapper.impl.InvoiceBatchMapperImpl;
import com.rbkmoney.reporter.mapper.impl.PaymentInvoiceBatchMapperImpl;
import com.rbkmoney.reporter.mapper.impl.RefundInvoiceBatchMapperImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceBatchHandlerConfig {

    @Bean
    public InvoiceBatchHandler<Invoice, Void> invoiceBatchHandler(InvoiceBatchManager invoiceBatchManager,
                                                                  InvoiceBatchMapperImpl invoiceBatchMapper) {
        return new InvoiceBatchHandlerImpl<>(InvoiceBatchType.INVOICE, invoiceBatchManager, invoiceBatchMapper);
    }

    @Bean
    public InvoiceBatchHandler<Payment, Invoice> paymentInvoiceBatchHandler(InvoiceBatchManager invoiceBatchManager,
                                                                            PaymentInvoiceBatchMapperImpl invoiceBatchMapper) {
        return new InvoiceBatchHandlerImpl<>(InvoiceBatchType.PAYMENT, invoiceBatchManager, invoiceBatchMapper);
    }

    @Bean
    public InvoiceBatchHandler<Adjustment, Invoice> adjustmentInvoiceBatchHandler(InvoiceBatchManager invoiceBatchManager,
                                                                                  AdjustmentInvoiceBatchMapperImpl invoiceBatchMapper) {
        return new InvoiceBatchHandlerImpl<>(InvoiceBatchType.ADJUSTMENT, invoiceBatchManager, invoiceBatchMapper);
    }

    @Bean
    public InvoiceBatchHandler<Refund, Payment> refundInvoiceBatchHandler(InvoiceBatchManager invoiceBatchManager,
                                                                          RefundInvoiceBatchMapperImpl invoiceBatchMapper) {
        return new InvoiceBatchHandlerImpl<>(InvoiceBatchType.REFUND, invoiceBatchManager, invoiceBatchMapper);
    }
}
