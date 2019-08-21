package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.batch.impl.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.mapper.InvoiceBatchMapper;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.service.AdjustmentService;
import com.rbkmoney.reporter.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdjustmentInvoiceBatchMapperImpl implements InvoiceBatchMapper<Adjustment, Invoice> {

    private final InvoiceService invoiceService;
    private final AdjustmentService adjustmentService;

    @Override
    public Adjustment map(InvoiceChangeMapper mapper, MapperPayload payload, List<Adjustment> adjustments) {
        throw new UnsupportedOperationException("Adjustment need cache");
    }

    @Override
    public Adjustment map(InvoiceChangeMapper mapper, MapperPayload payload, List<Adjustment> adjustments, Map<InvoiceUniqueBatchKey, Invoice> consumerCache) {
        String invoiceId = payload.getMachineEvent().getSourceId();
        String paymentId = payload.getInvoiceChange().getInvoicePaymentChange().getId();
        String adjustmentId = payload.getInvoiceChange().getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getId();

        Adjustment adjustment = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId()).getAdjustment();

        if (!adjustments.isEmpty()) {
            Adjustment lastAdjustment = adjustments.get(adjustments.size() - 1);
            BeanUtils.copyProperties(lastAdjustment, adjustment, mapper.getIgnoreProperties());
        } else if (adjustment.getEventType() != InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED) {
            Adjustment lastAdjustment = adjustmentService.get(invoiceId, paymentId, adjustmentId);
            BeanUtils.copyProperties(lastAdjustment, adjustment, mapper.getIgnoreProperties());
        }

        if (adjustment.getEventType() == InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED) {
            Invoice invoice = consumerCache.computeIfAbsent(
                    new InvoiceUniqueBatchKeyImpl(invoiceId),
                    key -> {
                        PartyData partyData = invoiceService.getPartyData(invoiceId);

                        Invoice inv = new Invoice();
                        inv.setPartyId(partyData.getPartyId());
                        inv.setPartyShopId(partyData.getPartyShopId());
                        return inv;
                    }
            );

            adjustment.setPartyId(invoice.getPartyId());
            adjustment.setPartyShopId(invoice.getPartyShopId());
        }

        return adjustment;
    }
}
