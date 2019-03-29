package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentStarted;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.domain.enums.BankCardTokenProvider;
import com.rbkmoney.reporter.domain.enums.OnHoldExpiration;
import com.rbkmoney.reporter.domain.enums.*;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.handle.stockevent.event.change.InvoiceChangeEventsHandler;
import com.rbkmoney.reporter.service.InvoiceService;
import com.rbkmoney.reporter.service.PaymentService;
import com.rbkmoney.reporter.util.json.FinalCashFlowUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentStartedChangeEventHandler implements InvoiceChangeEventsHandler {

    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange()
                && change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStarted();
    }

    @Override
    public void handle(InvoiceChange change, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getProcessingEvent();

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        InvoicePaymentStarted invoicePaymentStarted = getInvoicePaymentStarted(invoicePaymentChange);
        InvoicePayment invoicePayment = invoicePaymentStarted.getPayment();

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSource().getInvoiceId();
        Payer payer = invoicePayment.getPayer();
        Cash cost = invoicePayment.getCost();
        InvoicePaymentFlow paymentFlow = invoicePayment.getFlow();
        InvoicePaymentStatus status = invoicePayment.getStatus();

        Invoice invoice = invoiceService.get(invoiceId);

        log.info("Start invoice payment created handling, paymentId={}, invoiceId={}", paymentId, invoiceId);

        Payment payment = new Payment();
        payment.setEventId(event.getId());
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payment.setEventType(InvoiceEventType.INVOICE_PAYMENT_STARTED);
        payment.setSequenceId(event.getSequence());
        payment.setInvoiceId(invoiceId);
        payment.setPartyId(invoice.getPartyId());
        payment.setPartyShopId(invoice.getPartyShopId());
        payment.setPaymentId(paymentId);
        payment.setPaymentCreatedAt(TypeUtil.stringToLocalDateTime(invoicePayment.getCreatedAt()));
        payment.setPaymentDomainRevision(invoicePayment.getDomainRevision());
        if (invoicePayment.isSetPartyRevision()) {
            payment.setPaymentPartyRevision(invoicePayment.getPartyRevision());
        }
        payment.setPaymentStatus(TBaseUtil.unionFieldToEnum(status, com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus.class));
        fillPayer(payer, payment);
        fillCost(cost, payment);
        fillInvoicePaymentFlow(paymentFlow, payment);
        if (invoicePayment.isSetMakeRecurrent()) {
            payment.setPaymentMakeRecurrentFlag(invoicePayment.isMakeRecurrent());
        }
        fillInvoicePaymentContext(invoicePayment, payment);
        fillPaymentRoute(invoicePaymentStarted, payment);
        if (invoicePaymentStarted.isSetCashFlow()) {
            payment.setPaymentCashFlow(FinalCashFlowUtil.toDtoFinalCashFlow(invoicePaymentStarted.getCashFlow()));
        }

        paymentService.save(payment);
        log.info("Invoice payment has been created, paymentId={}, invoiceId={}", paymentId, invoiceId);
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

    private void fillCost(Cash cost, Payment payment) {
        payment.setPaymentAmount(cost.getAmount());
        payment.setPaymentOriginAmount(cost.getAmount());
        payment.setPaymentCurrencyCode(cost.getCurrency().getSymbolicCode());
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

    private void fillPaymentRoute(InvoicePaymentStarted invoicePaymentStarted, Payment payment) {
        if (invoicePaymentStarted.isSetRoute()) {
            PaymentRoute paymentRoute = invoicePaymentStarted.getRoute();

            payment.setPaymentProviderId(paymentRoute.getProvider().getId());
            payment.setPaymentTerminalId(paymentRoute.getTerminal().getId());
        }
    }
}
