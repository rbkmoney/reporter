package com.rbkmoney.reporter.mapper;

import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import lombok.Data;

@Data
public class MapperResult {

    private Invoice invoice;
    private Payment payment;
    private Adjustment adjustment;
    private Refund refund;

    public MapperResult(Invoice invoice) {
        this.invoice = invoice;
    }

    public MapperResult(Payment payment) {
        this.payment = payment;
    }

    public MapperResult(Adjustment adjustment) {
        this.adjustment = adjustment;
    }

    public MapperResult(Refund refund) {
        this.refund = refund;
    }
}
