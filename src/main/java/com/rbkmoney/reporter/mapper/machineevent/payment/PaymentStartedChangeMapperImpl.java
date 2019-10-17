package com.rbkmoney.reporter.mapper.machineevent.payment;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentStarted;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.BankCardTokenProvider;
import com.rbkmoney.reporter.domain.enums.OnHoldExpiration;
import com.rbkmoney.reporter.domain.enums.*;
import com.rbkmoney.reporter.domain.tables.pojos.*;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.CashFlowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PaymentStartedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentStarted invoicePaymentStarted = getInvoicePaymentStarted(invoicePaymentChange);
        InvoicePayment invoicePayment = invoicePaymentStarted.getPayment();

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();
        Payer payer = invoicePayment.getPayer();
        InvoicePaymentFlow paymentFlow = invoicePayment.getFlow();
        InvoicePaymentStatus status = invoicePayment.getStatus();

        Payment payment = new Payment();
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        payment.setEventType(InvoiceEventType.INVOICE_PAYMENT_STARTED);
        payment.setInvoiceId(invoiceId);
        payment.setSequenceId(baseEvent.getEventId());
        payment.setChangeId(changeId);
        payment.setPaymentId(paymentId);
        payment.setPaymentCreatedAt(TypeUtil.stringToLocalDateTime(invoicePayment.getCreatedAt()));
        payment.setPaymentDomainRevision(invoicePayment.getDomainRevision());

        fillPayer(payer, payment);
        fillInvoicePaymentFlow(paymentFlow, payment);
        fillInvoicePaymentContext(invoicePayment, payment);

        if (invoicePayment.isSetMakeRecurrent()) {
            payment.setPaymentMakeRecurrentFlag(invoicePayment.isMakeRecurrent());
        }
        if (invoicePayment.isSetPartyRevision()) {
            payment.setPaymentPartyRevision(invoicePayment.getPartyRevision());
        }

        PaymentState paymentState = getPaymentState(baseEvent, changeId, paymentId, status);
        PaymentCost paymentCost = getPaymentCost(baseEvent, changeId, paymentId, invoicePayment.getCost());
        PaymentRouting paymentRouting = getPaymentRouting(baseEvent, changeId, paymentId, invoicePaymentStarted);
        List<CashFlow> cashFlowList = getCashFlowList(baseEvent, changeId, paymentId, invoicePaymentStarted);

        log.info("Payment with eventType=created has been mapped, invoiceId={}, paymentId={}", invoiceId, paymentId);

        return new MapperResult(payment, paymentState, paymentCost, paymentRouting, cashFlowList);
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStarted();
    }

    @Override
    public String[] getIgnoreProperties() {
        return new String[0];
    }

    private PaymentCost getPaymentCost(MachineEvent baseEvent, Integer changeId, String paymentId, Cash cost) {
        PaymentCost paymentCost = new PaymentCost();
        paymentCost.setInvoiceId(baseEvent.getSourceId());
        paymentCost.setSequenceId(baseEvent.getEventId());
        paymentCost.setChangeId(changeId);
        paymentCost.setPaymentId(paymentId);
        paymentCost.setCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        paymentCost.setAmount(cost.getAmount());
        paymentCost.setOriginAmount(cost.getAmount());
        paymentCost.setCurrency(cost.getCurrency().getSymbolicCode());
        return paymentCost;
    }

    private PaymentState getPaymentState(MachineEvent baseEvent, Integer changeId, String paymentId, InvoicePaymentStatus status) {
        PaymentState state = new PaymentState();
        state.setInvoiceId(baseEvent.getSourceId());
        state.setSequenceId(baseEvent.getEventId());
        state.setChangeId(changeId);
        state.setPaymentId(paymentId);
        state.setCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        state.setPaymentStatus(TBaseUtil.unionFieldToEnum(status, com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus.class));
        return state;
    }

    private PaymentRouting getPaymentRouting(MachineEvent baseEvent,
                                             Integer changeId,
                                             String paymentId,
                                             InvoicePaymentStarted invoicePaymentStarted) {
        if (invoicePaymentStarted.isSetRoute()) {
            PaymentRoute paymentRoute = invoicePaymentStarted.getRoute();
            PaymentRouting paymentRouting = new PaymentRouting();
            paymentRouting.setInvoiceId(baseEvent.getSourceId());
            paymentRouting.setSequenceId(baseEvent.getEventId());
            paymentRouting.setChangeId(changeId);
            paymentRouting.setPaymentId(paymentId);
            paymentRouting.setCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
            paymentRouting.setProviderId(paymentRoute.getProvider().getId());
            paymentRouting.setTerminalId(paymentRoute.getTerminal().getId());
            return paymentRouting;
        }
        return null;
    }

    private List<CashFlow> getCashFlowList(MachineEvent baseEvent,
                                           Integer changeId,
                                           String paymentId,
                                           InvoicePaymentStarted invoicePaymentStarted) {
        if (invoicePaymentStarted.isSetCashFlow()) {
            return CashFlowUtil.convertCashFlows(
                    invoicePaymentStarted.getCashFlow(),
                    baseEvent.getSourceId(),
                    baseEvent.getEventId(),
                    changeId,
                    paymentId,
                    TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()),
                    PaymentChangeType.payment
            );
        }
        return null;
    }

    private InvoicePaymentStarted getInvoicePaymentStarted(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentStarted();
    }

    private void fillPayer(Payer payer, Payment payment) {
        payment.setPaymentPayerType(TBaseUtil.unionFieldToEnum(payer, PaymentPayerType.class));
        if (payer.isSetPaymentResource()) {
            PaymentResourcePayer paymentResource = payer.getPaymentResource();
            DisposablePaymentResource resource = paymentResource.getResource();
            PaymentTool paymentTool = resource.getPaymentTool();
            ContactInfo contactInfo = paymentResource.getContactInfo();

            fillPaymentToolUnion(payment, paymentTool);
            if (resource.isSetPaymentSessionId()) {
                payment.setPaymentSessionId(resource.getPaymentSessionId());
            }
            if (resource.isSetClientInfo()) {
                ClientInfo clientInfo = resource.getClientInfo();
                payment.setPaymentFingerprint(clientInfo.getFingerprint());
                payment.setPaymentIp(clientInfo.getIpAddress());
            }
            fillContactInfo(payment, contactInfo);
        } else if (payer.isSetCustomer()) {
            CustomerPayer customer = payer.getCustomer();
            PaymentTool paymentTool = customer.getPaymentTool();
            ContactInfo contactInfo = customer.getContactInfo();

            payment.setPaymentCustomerId(customer.getCustomerId());
            fillPaymentToolUnion(payment, paymentTool);
            fillContactInfo(payment, contactInfo);
        } else if (payer.isSetRecurrent()) {
            RecurrentPayer recurrent = payer.getRecurrent();
            RecurrentParentPayment recurrentParent = recurrent.getRecurrentParent();
            PaymentTool paymentTool = recurrent.getPaymentTool();
            ContactInfo contactInfo = recurrent.getContactInfo();

            fillPaymentToolUnion(payment, paymentTool);
            payment.setPaymentRecurrentPayerParentInvoiceId(recurrentParent.getInvoiceId());
            payment.setPaymentRecurrentPayerParentPaymentId(recurrentParent.getPaymentId());
            fillContactInfo(payment, contactInfo);
        }
    }

    private void fillPaymentToolUnion(Payment payment, PaymentTool paymentTool) {
        payment.setPaymentTool(TBaseUtil.unionFieldToEnum(paymentTool, com.rbkmoney.reporter.domain.enums.PaymentTool.class));
        if (paymentTool.isSetBankCard()) {
            BankCard bankCard = paymentTool.getBankCard();

            payment.setPaymentBankCardToken(bankCard.getToken());
            payment.setPaymentBankCardSystem(bankCard.getPaymentSystem().toString());
            payment.setPaymentBankCardBin(bankCard.getBin());
            payment.setPaymentBankCardMaskedPan(bankCard.getMaskedPan());
            if (bankCard.isSetTokenProvider()) {
                payment.setPaymentBankCardTokenProvider(TypeUtil.toEnumField(bankCard.getTokenProvider().name(), BankCardTokenProvider.class));
            }
        } else if (paymentTool.isSetPaymentTerminal()) {
            PaymentTerminal paymentTerminal = paymentTool.getPaymentTerminal();

            payment.setPaymentTerminalProvider(paymentTerminal.getTerminalType().toString());
        } else if (paymentTool.isSetDigitalWallet()) {
            DigitalWallet digitalWallet = paymentTool.getDigitalWallet();

            payment.setPaymentDigitalWalletId(digitalWallet.getId());
            payment.setPaymentDigitalWalletProvider(digitalWallet.getProvider().toString());
        }
    }

    private void fillContactInfo(Payment payment, ContactInfo contactInfo) {
        payment.setPaymentPhoneNumber(contactInfo.getPhoneNumber());
        payment.setPaymentEmail(contactInfo.getEmail());
    }

    private void fillInvoicePaymentFlow(InvoicePaymentFlow paymentFlow, Payment payment) {
        payment.setPaymentFlow(TBaseUtil.unionFieldToEnum(paymentFlow, PaymentFlow.class));
        if (paymentFlow.isSetHold()) {
            InvoicePaymentFlowHold hold = paymentFlow.getHold();

            payment.setPaymentHoldOnExpiration(OnHoldExpiration.valueOf(hold.getOnHoldExpiration().name()));
            payment.setPaymentHoldUntil(TypeUtil.stringToLocalDateTime(hold.getHeldUntil()));
        }
    }

    private void fillInvoicePaymentContext(InvoicePayment invoicePayment, Payment payment) {
        if (invoicePayment.isSetContext()) {
            Content content = invoicePayment.getContext();

            payment.setPaymentContextType(content.getType());
            payment.setPaymentContext(content.getData());
        }
    }

}
