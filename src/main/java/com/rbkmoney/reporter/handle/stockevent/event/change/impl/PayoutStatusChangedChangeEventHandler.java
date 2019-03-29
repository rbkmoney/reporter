package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.damsel.payout_processing.PayoutChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.domain.enums.PayoutEventType;
import com.rbkmoney.reporter.domain.enums.PayoutStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.handle.stockevent.event.change.PayoutChangeEventsHandler;
import com.rbkmoney.reporter.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayoutStatusChangedChangeEventHandler implements PayoutChangeEventsHandler {

    private final PayoutService payoutService;

    @Override
    public boolean accept(PayoutChange specific) {
        return specific.isSetPayoutStatusChanged();
    }

    @Override
    public void handle(PayoutChange specific, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getPayoutEvent();

        com.rbkmoney.damsel.payout_processing.PayoutStatus damselPayoutStatus = specific.getPayoutStatusChanged().getStatus();
        String payoutId = event.getSource().getPayoutId();

        log.info("Start payout status changed handling, payoutId={}", payoutId);

        Payout payout = payoutService.get(payoutId);

        payout.setEventId(event.getId());
        payout.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payout.setEventType(PayoutEventType.PAYOUT_STATUS_CHANGED);
//        payout.setSequenceId(event.getSequence());
        payout.setPayoutStatus(TBaseUtil.unionFieldToEnum(damselPayoutStatus, PayoutStatus.class));
        if (damselPayoutStatus.isSetCancelled()) {
            payout.setPayoutCancelDetails(damselPayoutStatus.getCancelled().getDetails());
        }

        payoutService.updateNotCurrent(payoutId);
        payoutService.save(payout);
        log.info("Payout status has been changed, payoutId={}", payoutId);
    }
}
