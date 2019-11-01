package com.rbkmoney.reporter.mapper;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;

public interface InvoiceChangeMapper {

    boolean canMap(InvoiceChange payload);

    MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId);

}
