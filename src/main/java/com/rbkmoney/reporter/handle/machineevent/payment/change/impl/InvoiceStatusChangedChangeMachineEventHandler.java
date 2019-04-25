package com.rbkmoney.reporter.handle.machineevent.payment.change.impl;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoiceStatusChanged;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.InvoiceStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.handle.machineevent.payment.change.InvoiceChangeMachineEventHandler;
import com.rbkmoney.reporter.service.InvoiceService;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvoiceStatusChangedChangeMachineEventHandler implements InvoiceChangeMachineEventHandler {

    private final InvoiceService invoiceService;

    @Override
    public boolean accept(InvoiceChange payload) {
        return payload.isSetInvoiceStatusChanged();
    }

    @Override
    public void handle(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoiceStatusChanged invoiceStatusChanged = payload.getInvoiceStatusChanged();
        com.rbkmoney.damsel.domain.InvoiceStatus invoiceStatus = invoiceStatusChanged.getStatus();

        String invoiceId = baseEvent.getSourceId();

        log.info("Start invoice status changed handling, invoiceId={}", invoiceId);

        Invoice invoice = invoiceService.get(invoiceId);

        invoice.setId(null);
        invoice.setWtime(null);
        invoice.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        invoice.setEventType(InvoiceEventType.INVOICE_STATUS_CHANGED);
        invoice.setSequenceId(baseEvent.getEventId());
        invoice.setChangeId(changeId);
        invoice.setInvoiceStatus(TBaseUtil.unionFieldToEnum(invoiceStatus, InvoiceStatus.class));
        invoice.setInvoiceStatusDetails(DamselUtil.getInvoiceStatusDetails(invoiceStatus));

        invoiceService.updateNotCurrent(invoiceId);
        invoiceService.save(invoice);
        log.info("Invoice status has been changed, invoiceId={}", invoiceId);
    }
}
