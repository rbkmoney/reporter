package com.rbkmoney.reporter.config;

import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.handler.invoicing.InvoiceBatchHandler;
import com.rbkmoney.reporter.handler.invoicing.InvoiceBatchHandlerImpl;
import com.rbkmoney.reporter.mapper.impl.AdjustmentQueryInvoiceBatchMapperImpl;
import com.rbkmoney.reporter.mapper.impl.InvoiceQueryInvoiceBatchMapperImpl;
import com.rbkmoney.reporter.mapper.impl.PaymentQueryInvoiceBatchMapperImpl;
import com.rbkmoney.reporter.mapper.impl.RefundQueryInvoiceBatchMapperImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceBatchHandlerConfig {

    @Bean
    public InvoiceBatchHandler<PartyData, Void> invoiceBatchHandler(InvoiceBatchManager invoiceBatchManager,
                                                                    InvoiceQueryInvoiceBatchMapperImpl invoiceBatchMapper) {
        return new InvoiceBatchHandlerImpl<>(InvoiceBatchType.INVOICE, invoiceBatchManager, invoiceBatchMapper);
    }

    @Bean
    public InvoiceBatchHandler<PaymentPartyData, PartyData> paymentInvoiceBatchHandler(InvoiceBatchManager invoiceBatchManager,
                                                                                       PaymentQueryInvoiceBatchMapperImpl invoiceBatchMapper) {
        return new InvoiceBatchHandlerImpl<>(InvoiceBatchType.PAYMENT, invoiceBatchManager, invoiceBatchMapper);
    }

    @Bean
    public InvoiceBatchHandler<Void, PaymentPartyData> adjustmentInvoiceBatchHandler(InvoiceBatchManager invoiceBatchManager,
                                                                                     AdjustmentQueryInvoiceBatchMapperImpl invoiceBatchMapper) {
        return new InvoiceBatchHandlerImpl<>(InvoiceBatchType.ADJUSTMENT, invoiceBatchManager, invoiceBatchMapper);
    }

    @Bean
    public InvoiceBatchHandler<Void, PaymentPartyData> refundInvoiceBatchHandler(InvoiceBatchManager invoiceBatchManager,
                                                                                 RefundQueryInvoiceBatchMapperImpl invoiceBatchMapper) {
        return new InvoiceBatchHandlerImpl<>(InvoiceBatchType.REFUND, invoiceBatchManager, invoiceBatchMapper);
    }
}
