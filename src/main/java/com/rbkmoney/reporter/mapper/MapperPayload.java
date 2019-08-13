package com.rbkmoney.reporter.mapper;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MapperPayload {

    private MachineEvent machineEvent;
    private InvoiceChange invoiceChange;
    private Integer changeId;

}
