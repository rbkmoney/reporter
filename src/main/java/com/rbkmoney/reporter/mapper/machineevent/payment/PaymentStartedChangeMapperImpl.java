package com.rbkmoney.reporter.mapper.machineevent.payment;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentStarted;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.BankCardTokenProvider;
import com.rbkmoney.reporter.domain.enums.OnHoldExpiration;
import com.rbkmoney.reporter.domain.enums.PaymentFlow;
import com.rbkmoney.reporter.domain.enums.PaymentPayerType;
import com.rbkmoney.reporter.domain.tables.pojos.*;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.FeeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.reporter.util.DamselUtil.getCurrency;
import static com.rbkmoney.reporter.util.DamselUtil.getFees;
import static com.rbkmoney.reporter.util.FeeTypeMapUtil.isContainsAnyFee;
import static com.rbkmoney.reporter.util.MapperUtil.*;

@Component
@Slf4j
public class PaymentStartedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStarted();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange damselPaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentStarted damselPaymentStarted = getInvoicePaymentStarted(damselPaymentChange);
        InvoicePayment damselPayment = damselPaymentStarted.getPayment();

        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());
        String paymentId = damselPaymentChange.getId();
        Payer payer = damselPayment.getPayer();
        InvoicePaymentFlow paymentFlow = damselPayment.getFlow();
        InvoicePaymentStatus status = damselPayment.getStatus();
        Cash cost = damselPayment.getCost();

        Payment payment = getPayment(damselPayment, invoiceId, paymentId, payer, paymentFlow);

        PaymentState paymentState = getPaymentState(invoiceId, changeId, sequenceId, eventCreatedAt, paymentId, status);

        PaymentCost paymentCost = getPaymentCost(invoiceId, sequenceId, changeId, eventCreatedAt, paymentId, cost.getAmount(), cost.getCurrency().getSymbolicCode());

        PaymentRouting paymentRouting = null;
        if (damselPaymentStarted.isSetRoute()) {
            PaymentRoute paymentRoute = damselPaymentStarted.getRoute();
            paymentRouting = getPaymentRouting(invoiceId, changeId, sequenceId, eventCreatedAt, paymentId, paymentRoute);
        }

        PaymentFee paymentFee = null;
        if (damselPaymentStarted.isSetCashFlow()) {
            List<FinalCashFlowPosting> cashFlowPostings = damselPaymentStarted.getCashFlow();
            Map<FeeType, Long> fees = getFees(cashFlowPostings);
            Map<FeeType, String> currencies = getCurrency(cashFlowPostings);

            if (isContainsAnyFee(fees)) {
                paymentFee = getPaymentFee(invoiceId, changeId, sequenceId, eventCreatedAt, paymentId, fees, currencies);
            }
        }

        log.info("Payment with eventType=[created] has been mapped, invoiceId={}, paymentId={}", invoiceId, paymentId);

        return new MapperResult(payment, paymentState, paymentCost, paymentRouting, paymentFee);
    }

    private Payment getPayment(InvoicePayment damselPayment, String invoiceId, String paymentId, Payer payer, InvoicePaymentFlow paymentFlow) {
        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setPaymentId(paymentId);
        payment.setCreatedAt(TypeUtil.stringToLocalDateTime(damselPayment.getCreatedAt()));
        payment.setDomainRevision(damselPayment.getDomainRevision());
        if (damselPayment.isSetPartyRevision()) {
            payment.setPartyRevision(damselPayment.getPartyRevision());
        }
        fillPayer(payer, payment);
        fillInvoicePaymentFlow(paymentFlow, payment);
        if (damselPayment.isSetMakeRecurrent()) {
            payment.setMakeRecurrentFlag(damselPayment.isMakeRecurrent());
        }
        fillInvoicePaymentContext(damselPayment, payment);

        return payment;
    }

    private InvoicePaymentStarted getInvoicePaymentStarted(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentStarted();
    }

    private void fillPayer(Payer payer, Payment payment) {
        payment.setPayerType(TBaseUtil.unionFieldToEnum(payer, PaymentPayerType.class));
        if (payer.isSetPaymentResource()) {
            PaymentResourcePayer paymentResource = payer.getPaymentResource();
            DisposablePaymentResource resource = paymentResource.getResource();
            PaymentTool paymentTool = resource.getPaymentTool();
            ContactInfo contactInfo = paymentResource.getContactInfo();

            fillPaymentToolUnion(payment, paymentTool);
            if (resource.isSetPaymentSessionId()) {
                payment.setSessionId(resource.getPaymentSessionId());
            }
            if (resource.isSetClientInfo()) {
                ClientInfo clientInfo = resource.getClientInfo();
                payment.setFingerprint(clientInfo.getFingerprint());
                payment.setIp(clientInfo.getIpAddress());
            }
            fillContactInfo(payment, contactInfo);
        } else if (payer.isSetCustomer()) {
            CustomerPayer customer = payer.getCustomer();
            PaymentTool paymentTool = customer.getPaymentTool();
            ContactInfo contactInfo = customer.getContactInfo();

            payment.setCustomerId(customer.getCustomerId());
            fillPaymentToolUnion(payment, paymentTool);
            fillContactInfo(payment, contactInfo);
        } else if (payer.isSetRecurrent()) {
            RecurrentPayer recurrent = payer.getRecurrent();
            RecurrentParentPayment recurrentParent = recurrent.getRecurrentParent();
            PaymentTool paymentTool = recurrent.getPaymentTool();
            ContactInfo contactInfo = recurrent.getContactInfo();

            fillPaymentToolUnion(payment, paymentTool);
            payment.setRecurrentPayerParentInvoiceId(recurrentParent.getInvoiceId());
            payment.setRecurrentPayerParentPaymentId(recurrentParent.getPaymentId());
            fillContactInfo(payment, contactInfo);
        }
    }

    private void fillPaymentToolUnion(Payment payment, PaymentTool paymentTool) {
        payment.setTool(TBaseUtil.unionFieldToEnum(paymentTool, com.rbkmoney.reporter.domain.enums.PaymentTool.class));
        if (paymentTool.isSetBankCard()) {
            BankCard bankCard = paymentTool.getBankCard();

            payment.setBankCardToken(bankCard.getToken());
            payment.setBankCardSystem(bankCard.getPaymentSystem().toString());
            payment.setBankCardBin(bankCard.getBin());
            payment.setBankCardMaskedPan(bankCard.getLastDigits());
            if (bankCard.isSetTokenProvider()) {
                payment.setBankCardTokenProvider(TypeUtil.toEnumField(bankCard.getTokenProvider().name(), BankCardTokenProvider.class));
            }
        } else if (paymentTool.isSetPaymentTerminal()) {
            PaymentTerminal paymentTerminal = paymentTool.getPaymentTerminal();

            payment.setTerminalProvider(paymentTerminal.getTerminalType().toString());
        } else if (paymentTool.isSetDigitalWallet()) {
            DigitalWallet digitalWallet = paymentTool.getDigitalWallet();

            payment.setDigitalWalletId(digitalWallet.getId());
            payment.setDigitalWalletProvider(digitalWallet.getProvider().toString());
        }
    }

    private void fillContactInfo(Payment payment, ContactInfo contactInfo) {
        payment.setPhoneNumber(contactInfo.getPhoneNumber());
        payment.setEmail(contactInfo.getEmail());
    }

    private void fillInvoicePaymentFlow(InvoicePaymentFlow paymentFlow, Payment payment) {
        payment.setFlow(TBaseUtil.unionFieldToEnum(paymentFlow, PaymentFlow.class));
        if (paymentFlow.isSetHold()) {
            InvoicePaymentFlowHold hold = paymentFlow.getHold();

            payment.setHoldOnExpiration(OnHoldExpiration.valueOf(hold.getOnHoldExpiration().name()));
            payment.setHoldUntil(TypeUtil.stringToLocalDateTime(hold.getHeldUntil()));
        }
    }

    private void fillInvoicePaymentContext(InvoicePayment invoicePayment, Payment payment) {
        if (invoicePayment.isSetContext()) {
            Content content = invoicePayment.getContext();

            payment.setContextType(content.getType());
            payment.setContext(content.getData());
        }
    }

}
