package com.rbkmoney.reporter.handler.payout;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.damsel.payout_processing.PayoutChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.dao.query.PayoutQueryTemplator;
import com.rbkmoney.reporter.domain.enums.PayoutStatus;
import com.rbkmoney.reporter.domain.tables.pojos.PayoutState;
import com.rbkmoney.reporter.service.BatchService;
import com.rbkmoney.sink.common.handle.stockevent.event.change.PayoutChangeEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayoutStatusChangedChangeEventHandler implements PayoutChangeEventHandler {

    private final PayoutQueryTemplator payoutQueryTemplator;
    private final BatchService batchService;

    @Override
    public void handle(PayoutChange payload,
                       StockEvent baseEvent,
                       Integer changeId) {
        Event event = baseEvent.getSourceEvent().getPayoutEvent();

        var damselPayoutStatus = payload.getPayoutStatusChanged().getStatus();
        String payoutId = event.getSource().getPayoutId();

        log.info("Start payout status changed handling, payoutId={}", payoutId);

        PayoutState payoutState = new PayoutState();
        payoutState.setEventId(event.getId());
        payoutState.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payoutState.setPayoutId(payoutId);
        payoutState.setStatus(TBaseUtil.unionFieldToEnum(damselPayoutStatus, PayoutStatus.class));
        if (damselPayoutStatus.isSetCancelled()) {
            payoutState.setCancelDetails(damselPayoutStatus.getCancelled().getDetails());
        }

        Query savePayoutStateQuery = payoutQueryTemplator.getSavePayoutStateQuery(payoutState);

        batchService.save(List.of(savePayoutStateQuery));

        log.info("Payout status has been changed, payoutId={}", payoutId);
    }

    @Override
    public boolean accept(PayoutChange payload) {
        return payload.isSetPayoutStatusChanged();
    }
}
