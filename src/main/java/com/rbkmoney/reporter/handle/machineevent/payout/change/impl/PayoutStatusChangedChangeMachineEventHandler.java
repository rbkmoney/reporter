package com.rbkmoney.reporter.handle.machineevent.payout.change.impl;

import com.rbkmoney.damsel.payout_processing.PayoutChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.PayoutEventType;
import com.rbkmoney.reporter.domain.enums.PayoutStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.handle.machineevent.payout.change.PayoutChangeMachineEventHandler;
import com.rbkmoney.reporter.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayoutStatusChangedChangeMachineEventHandler implements PayoutChangeMachineEventHandler {

    private final PayoutService payoutService;

    @Override
    public boolean accept(PayoutChange payload) {
        return payload.isSetPayoutStatusChanged();
    }

    @Override
    public void handle(PayoutChange payload, MachineEvent baseEvent) {
        com.rbkmoney.damsel.payout_processing.PayoutStatus damselPayoutStatus = payload.getPayoutStatusChanged().getStatus();

        String payoutId = baseEvent.getSourceId();

        log.info("Start payout status changed handling, payoutId={}", payoutId);

        Payout payout = payoutService.get(payoutId);

        payout.setId(null);
        payout.setWtime(null);
        payout.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        payout.setEventType(PayoutEventType.PAYOUT_STATUS_CHANGED);
        payout.setPayoutStatus(TBaseUtil.unionFieldToEnum(damselPayoutStatus, PayoutStatus.class));
        if (damselPayoutStatus.isSetCancelled()) {
            payout.setPayoutCancelDetails(damselPayoutStatus.getCancelled().getDetails());
        }

        payoutService.updateNotCurrent(payoutId);
        payoutService.save(payout);
        log.info("Payout status has been changed, payoutId={}", payoutId);
    }
}
