package com.rbkmoney.reporter.util;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentSession;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentStatusChanged;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.mamsel.DigitalWalletUtil;
import com.rbkmoney.mamsel.PaymentSystemUtil;
import com.rbkmoney.mamsel.TerminalPaymentUtil;
import com.rbkmoney.mamsel.TokenProviderUtil;
import com.rbkmoney.reporter.domain.enums.*;
import com.rbkmoney.reporter.domain.enums.BankCardTokenProvider;
import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.enums.InvoiceStatus;
import com.rbkmoney.reporter.domain.enums.OnHoldExpiration;
import com.rbkmoney.reporter.domain.tables.pojos.*;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.model.FeeType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rbkmoney.reporter.util.DamselUtil.getFees;
import static com.rbkmoney.reporter.util.FeeTypeMapUtil.isContainsAmount;
import static com.rbkmoney.reporter.util.FeeTypeMapUtil.isContainsAnyFee;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapperUtils {

    public static Refund createRefundRecord(com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund hgRefund,
                                            MachineEvent event,
                                            com.rbkmoney.damsel.payment_processing.Invoice hgInvoice,
                                            InvoicePayment invoicePayment) {
        var hgInnerInvoice = hgInvoice.getInvoice();
        var hgInnerPayment = invoicePayment.getPayment();
        InvoicePaymentRefund hgInnerRefund = hgRefund.getRefund();
        Map<FeeType, String> currencies = DamselUtil.getCurrency(hgRefund.getCashFlow());

        Refund refund = new Refund();
        refund.setExternalId(hgInnerRefund.getExternalId());
        refund.setPartyId(hgInnerInvoice.getOwnerId());
        refund.setShopId(hgInnerInvoice.getShopId());
        refund.setInvoiceId(event.getSourceId());
        refund.setPaymentId(hgInnerPayment.getId());
        refund.setRefundId(hgInnerRefund.getId());
        refund.setCreatedAt(TypeUtil.stringToLocalDateTime(hgInnerRefund.getCreatedAt()));
        if (hgInnerRefund.isSetCash()) {
            Cash cash = hgInnerRefund.getCash();
            refund.setAmount(cash.getAmount());
            refund.setCurrencyCode(cash.getCurrency().getSymbolicCode());
        }
        Map<FeeType, Long> fees = DamselUtil.getFees(hgRefund.getCashFlow());
        if (refund.getAmount() == null && isContainsAmount(fees)) {
            refund.setAmount(fees.get(FeeType.AMOUNT));
            refund.setCurrencyCode(currencies.get(FeeType.AMOUNT));
        }

        refund.setReason(hgInnerRefund.getReason());
        refund.setStatus(TBaseUtil.unionFieldToEnum(hgInnerRefund.getStatus(), RefundStatus.class));
        refund.setStatusCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        refund.setFee(fees.get(FeeType.FEE));
        refund.setProviderFee(fees.get(FeeType.PROVIDER_FEE));
        refund.setExternalFee(fees.get(FeeType.EXTERNAL_FEE));
        return refund;
    }

    public static RefundAdditionalInfo createRefundAdditionalInfoRecord(
            com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund hgRefund,
            InvoicePaymentRefundStatus status,
            Long extRefundId
    ) {
        InvoicePaymentRefund hgInnerRefund = hgRefund.getRefund();
        RefundAdditionalInfo additionalInfo = new RefundAdditionalInfo();
        additionalInfo.setExtRefundId(extRefundId);
        additionalInfo.setDomainRevision(hgInnerRefund.getDomainRevision());
        additionalInfo.setPartyRevision(hgInnerRefund.getPartyRevision());

        if (status.isSetFailed()) {
            OperationFailure operationFailure = status.getFailed().getFailure();
            additionalInfo.setOperationFailureClass(TBaseUtil.unionFieldToEnum(operationFailure, FailureClass.class));
            if (operationFailure.isSetFailure()) {
                Failure failure = operationFailure.getFailure();
                additionalInfo.setExternalFailure(TErrorUtil.toStringVal(failure));
                additionalInfo.setExternalFailureReason(failure.getReason());
            }
        }
        return additionalInfo;
    }

    public static Chargeback createChargebackRecord(InvoicePaymentChargeback paymentChargeback,
                                                    InvoicePayment invoicePayment,
                                                    com.rbkmoney.damsel.payment_processing.Invoice hgInvoice,
                                                    MachineEvent event) {
        var hgInnerInvoice = hgInvoice.getInvoice();
        var hgInnerPayment = invoicePayment.getPayment();
        Chargeback chargeback = new Chargeback();
        chargeback.setDomainRevision(paymentChargeback.getDomainRevision());
        chargeback.setPartyRevision(paymentChargeback.getPartyRevision());
        chargeback.setInvoiceId(event.getSourceId());
        chargeback.setPaymentId(hgInnerPayment.getId());
        chargeback.setChargebackId(paymentChargeback.getId());
        chargeback.setShopId(hgInnerInvoice.getShopId());
        chargeback.setPartyId(hgInnerInvoice.getOwnerId());
        chargeback.setExternalId(paymentChargeback.getExternalId());
        chargeback.setEventCreatedAt(TypeUtil.stringToLocalDateTime(paymentChargeback.getCreatedAt()));
        chargeback.setCreatedAt(TypeUtil.stringToLocalDateTime(paymentChargeback.getCreatedAt()));
        chargeback.setStatus(
                TBaseUtil.unionFieldToEnum(paymentChargeback.getStatus(), ChargebackStatus.class));
        chargeback.setLevyAmount(paymentChargeback.getLevy().getAmount());
        chargeback.setLevyCurrencyCode(paymentChargeback.getLevy().getCurrency().getSymbolicCode());
        chargeback.setAmount(paymentChargeback.getBody().getAmount());
        chargeback.setCurrencyCode(paymentChargeback.getBody().getCurrency().getSymbolicCode());
        chargeback.setReasonCode(paymentChargeback.getReason().getCode());
        chargeback.setReasonCategory(TBaseUtil.unionFieldToEnum(
                paymentChargeback.getReason().getCategory(),
                ChargebackCategory.class)
        );
        chargeback.setStage(TBaseUtil.unionFieldToEnum(paymentChargeback.getStage(), ChargebackStage.class));
        chargeback.setContext(Optional.ofNullable(paymentChargeback.getContext()).map(Content::getData).orElse(null));
        return chargeback;
    }

    public static Adjustment createAdjustmentRecord(InvoicePaymentAdjustment paymentAdjustment,
                                                    InvoicePayment invoicePayment,
                                                    com.rbkmoney.damsel.payment_processing.Invoice hgInvoice,
                                                    MachineEvent event) {
        var hgInnerInvoice = hgInvoice.getInvoice();
        var hgInnerPayment = invoicePayment.getPayment();
        Adjustment adjustment = new Adjustment();
        adjustment.setPartyId(hgInnerInvoice.getOwnerId());
        adjustment.setShopId(hgInnerInvoice.getShopId());
        adjustment.setInvoiceId(event.getSourceId());
        adjustment.setPaymentId(hgInnerPayment.getId());
        adjustment.setAdjustmentId(paymentAdjustment.getId());
        adjustment.setCreatedAt(TypeUtil.stringToLocalDateTime(paymentAdjustment.getCreatedAt()));
        adjustment.setStatus(TBaseUtil.unionFieldToEnum(paymentAdjustment.getStatus(), AdjustmentStatus.class));
        adjustment.setStatusCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        adjustment.setDomainRevision(paymentAdjustment.getDomainRevision());
        adjustment.setReason(paymentAdjustment.getReason());
        adjustment.setPartyRevision(paymentAdjustment.getPartyRevision());
        Long oldAmount = DamselUtil.computeMerchantAmount(paymentAdjustment.getOldCashFlowInverse());
        Long newAmount = DamselUtil.computeMerchantAmount(paymentAdjustment.getNewCashFlow());
        adjustment.setAmount(oldAmount + newAmount);
        adjustment.setCurrencyCode(DamselUtil.getCurrencyFromCashFlow(paymentAdjustment.getOldCashFlowInverse()));
        return adjustment;
    }

    public static Payment createPaymentRecord(MachineEvent event,
                                              com.rbkmoney.damsel.payment_processing.Invoice hgInvoice,
                                              InvoicePayment invoicePayment) {
        var hgInnerInvoice = hgInvoice.getInvoice();
        var hgInnerPayment = invoicePayment.getPayment();

        Payment payment = new Payment();
        payment.setInvoiceId(event.getSourceId());
        payment.setPaymentId(hgInnerPayment.getId());
        payment.setExternalId(hgInnerPayment.getExternalId());
        payment.setCreatedAt(TypeUtil.stringToLocalDateTime(hgInnerPayment.getCreatedAt()));
        payment.setStatus(TBaseUtil.unionFieldToEnum(hgInnerPayment.getStatus(), InvoicePaymentStatus.class));
        payment.setStatusCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));

        payment.setPartyId(hgInnerInvoice.getOwnerId());
        payment.setShopId(hgInnerInvoice.getShopId());
        payment.setFlow(TBaseUtil.unionFieldToEnum(hgInnerPayment.getFlow(), PaymentFlow.class));

        fillPayerInfo(hgInnerPayment.getPayer(), payment);

        if (hgInnerPayment.isSetCost()) {
            Cash cost = hgInnerPayment.getCost();
            payment.setAmount(cost.getAmount());
            payment.setOriginAmount(cost.getAmount());
            payment.setCurrencyCode(cost.getCurrency().getSymbolicCode());
        }

        if (invoicePayment.isSetCashFlow()) {
            fillCashFlow(invoicePayment.getCashFlow(), payment);
        }
        if (hgInnerPayment.isSetContext()) {
            Content context = hgInnerPayment.getContext();
            payment.setContext(context.getData());
            payment.setContextType(context.getType());
        }
        return payment;
    }

    public static PaymentAdditionalInfo createPaymentAdditionalInfoRecord(
            MachineEvent event,
            InvoicePayment invoicePayment,
            InvoicePaymentStatusChanged paymentStatusChanged,
            Long extPaymentId,
            int changeId
    ) {
        var hgInnerPayment = invoicePayment.getPayment();

        PaymentAdditionalInfo additionalInfo = new PaymentAdditionalInfo();
        additionalInfo.setExtPaymentId(extPaymentId);
        additionalInfo.setDomainRevision(hgInnerPayment.getDomainRevision());
        if (hgInnerPayment.isSetPartyRevision()) {
            additionalInfo.setPartyRevision(hgInnerPayment.getPartyRevision());
        }
        fillAdditionalPayerInfo(hgInnerPayment.getPayer(), additionalInfo);
        fillInvoicePaymentFlow(hgInnerPayment.getFlow(), additionalInfo);

        if (invoicePayment.isSetRoute()) {
            PaymentRoute route = invoicePayment.getRoute();
            additionalInfo.setProviderId(route.getProvider().getId());
            additionalInfo.setTerminalId(route.getTerminal().getId());
        }

        if (hgInnerPayment.isSetMakeRecurrent()) {
            additionalInfo.setMakeRecurrentFlag(hgInnerPayment.isMakeRecurrent());
        }
        // TODO: add after HG fix
        // additionalInfo.setPaymentShortId(hgInnerPayment);

        fillPaymentFailedStatusInfo(paymentStatusChanged, additionalInfo);

        if (invoicePayment.isSetSessions()) {
            fillSessionInfo(invoicePayment.getSessions(), event, changeId, additionalInfo);
        }
        return additionalInfo;
    }

    private static void fillPaymentFailedStatusInfo(InvoicePaymentStatusChanged paymentStatusChanged,
                                                    PaymentAdditionalInfo additionalInfo) {
        var paymentStatus = paymentStatusChanged.getStatus();
        if (paymentStatus.isSetFailed()) {
            OperationFailure operationFailure = paymentStatus.getFailed().getFailure();
            additionalInfo.setOperationFailureClass(TBaseUtil.unionFieldToEnum(operationFailure, FailureClass.class));
            if (operationFailure.isSetFailure()) {
                Failure failure = operationFailure.getFailure();
                additionalInfo.setExternalFailure(TErrorUtil.toStringVal(failure));
                additionalInfo.setExternalFailureReason(failure.getReason());
            }
        }
    }

    private static void fillSessionInfo(List<InvoicePaymentSession> sessions,
                                        MachineEvent event,
                                        int changeId,
                                        PaymentAdditionalInfo additionalInfo) {
        InvoicePaymentSession paymentSession = sessions.stream()
                .filter(session -> session.getTargetStatus().isSetCaptured()
                        || session.getTargetStatus().isSetCancelled())
                .findFirst()
                .orElse(null);
        if (paymentSession == null) {
            log.info("Session for transaction with invoice id '{}', sequence id '{}' and change id '{}' not found!",
                    event.getSourceId(), event.getEventId(), changeId);
            return;
        }

        if (paymentSession.isSetTransactionInfo()
                && paymentSession.getTransactionInfo().isSetAdditionalInfo()) {
            AdditionalTransactionInfo additionalTrxInfo =
                    paymentSession.getTransactionInfo().getAdditionalInfo();
            additionalInfo.setRrn(additionalTrxInfo.getRrn());
            additionalInfo.setApprovalCode(additionalTrxInfo.getApprovalCode());
            additionalInfo.setAcsUrl(additionalTrxInfo.getAcsUrl());
            additionalInfo.setPareq(additionalTrxInfo.getPareq());
            additionalInfo.setMd(additionalTrxInfo.getMd());
            additionalInfo.setTermUrl(additionalTrxInfo.getTermUrl());
            additionalInfo.setPares(additionalTrxInfo.getPares());
            additionalInfo.setEci(additionalTrxInfo.getEci());
            additionalInfo.setCavv(additionalTrxInfo.getCavv());
            additionalInfo.setCavvAlgorithm(additionalTrxInfo.getCavvAlgorithm());
            additionalInfo.setXid(additionalTrxInfo.getXid());
            additionalInfo.setThreeDsVerification(additionalTrxInfo.getThreeDsVerification() == null
                    ? null
                    : additionalTrxInfo.getThreeDsVerification().name());
        }
    }

    private static void fillInvoicePaymentFlow(InvoicePaymentFlow paymentFlow,
                                               PaymentAdditionalInfo additionalInfo) {
        if (paymentFlow.isSetHold()) {
            InvoicePaymentFlowHold hold = paymentFlow.getHold();

            additionalInfo.setHoldOnExpiration(OnHoldExpiration.valueOf(hold.getOnHoldExpiration().name()));
            additionalInfo.setHoldUntil(TypeUtil.stringToLocalDateTime(hold.getHeldUntil()));
        }
    }

    private static void fillAdditionalPayerInfo(Payer payer, PaymentAdditionalInfo additionalInfo) {
        if (payer.isSetPaymentResource()) {
            PaymentResourcePayer paymentResource = payer.getPaymentResource();
            DisposablePaymentResource resource = paymentResource.getResource();
            PaymentTool paymentTool = resource.getPaymentTool();

            fillPaymentToolUnion(additionalInfo, paymentTool);
            if (resource.isSetPaymentSessionId()) {
                additionalInfo.setSessionId(resource.getPaymentSessionId());
            }
            if (resource.isSetClientInfo()) {
                ClientInfo clientInfo = resource.getClientInfo();
                additionalInfo.setFingerprint(clientInfo.getFingerprint());
                additionalInfo.setIp(clientInfo.getIpAddress());
            }
        } else if (payer.isSetCustomer()) {
            CustomerPayer customer = payer.getCustomer();
            PaymentTool paymentTool = customer.getPaymentTool();

            additionalInfo.setCustomerId(customer.getCustomerId());
            fillPaymentToolUnion(additionalInfo, paymentTool);
        } else if (payer.isSetRecurrent()) {
            RecurrentPayer recurrent = payer.getRecurrent();
            RecurrentParentPayment recurrentParent = recurrent.getRecurrentParent();
            PaymentTool paymentTool = recurrent.getPaymentTool();

            fillPaymentToolUnion(additionalInfo, paymentTool);
            additionalInfo.setRecurrentPayerParentInvoiceId(recurrentParent.getInvoiceId());
            additionalInfo.setRecurrentPayerParentPaymentId(recurrentParent.getPaymentId());
        }
    }

    private static void fillPaymentToolUnion(PaymentAdditionalInfo additionalInfo,
                                             PaymentTool paymentTool) {
        if (paymentTool.isSetBankCard()) {
            BankCard bankCard = paymentTool.getBankCard();

            additionalInfo.setBankCardToken(bankCard.getToken());
            additionalInfo.setBankCardSystem(PaymentSystemUtil.getPaymentSystemName(bankCard));
            additionalInfo.setBankCardBin(bankCard.getBin());
            additionalInfo.setBankCardMaskedPan(bankCard.getLastDigits());
            additionalInfo.setBankCardTokenProviderRef(TokenProviderUtil.getTokenProviderName(bankCard));
        } else if (paymentTool.isSetPaymentTerminal()) {
            PaymentTerminal paymentTerminal = paymentTool.getPaymentTerminal();

            additionalInfo.setTerminalProvider(TerminalPaymentUtil.getTerminalPaymentProviderName(paymentTerminal));
        } else if (paymentTool.isSetDigitalWallet()) {
            DigitalWallet digitalWallet = paymentTool.getDigitalWallet();

            additionalInfo.setDigitalWalletId(digitalWallet.getId());
            additionalInfo.setDigitalWalletProvider(DigitalWalletUtil.getDigitalWalletName(digitalWallet));
        }
    }

    public static Invoice createInvoiceRecord(com.rbkmoney.damsel.payment_processing.Invoice hgInvoice,
                                              MachineEvent event) {
        var hgInnerInvoice = hgInvoice.getInvoice();
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(hgInnerInvoice.getId());
        invoice.setExternalId(hgInnerInvoice.getExternalId());
        invoice.setCreatedAt(TypeUtil.stringToLocalDateTime(hgInnerInvoice.getCreatedAt()));
        invoice.setAmount(hgInnerInvoice.getCost().getAmount());
        invoice.setCurrencyCode(hgInnerInvoice.getCost().getCurrency().getSymbolicCode());
        invoice.setDescription(hgInnerInvoice.getDetails().getDescription());
        invoice.setProduct(hgInnerInvoice.getDetails().getProduct());
        invoice.setStatus(TBaseUtil.unionFieldToEnum(hgInnerInvoice.getStatus(), InvoiceStatus.class));
        invoice.setStatusCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        invoice.setDue(TypeUtil.stringToLocalDateTime(hgInnerInvoice.getDue()));
        invoice.setPartyId(hgInnerInvoice.getOwnerId());
        invoice.setShopId(hgInnerInvoice.getShopId());
        return invoice;
    }

    public static InvoiceAdditionalInfo createInvoiceAdditionalInfoRecord(
            com.rbkmoney.damsel.payment_processing.Invoice hgInvoice,
            Long extInvoiceId
    ) {
        var hgInnerInvoice = hgInvoice.getInvoice();
        InvoiceAdditionalInfo additionalInfo = new InvoiceAdditionalInfo();
        additionalInfo.setExtInvoiceId(extInvoiceId);
        additionalInfo.setTemplateId(hgInnerInvoice.getTemplateId());
        additionalInfo.setStatusDetails(DamselUtil.getInvoiceStatusDetails(hgInnerInvoice.getStatus()));
        additionalInfo.setPartyRevision(hgInnerInvoice.getPartyRevision());
        additionalInfo.setContextType(hgInnerInvoice.getContext().getType());
        additionalInfo.setContext(hgInnerInvoice.getContext().getData());
        InvoiceDetails details = hgInnerInvoice.getDetails();
        if (details.isSetCart()) {
            additionalInfo.setCartJson(DamselUtil.toJsonString(details.getCart()));
        }

        return additionalInfo;
    }

    private static void fillCashFlow(List<FinalCashFlowPosting> cashFlow, Payment payment) {
        Map<FeeType, Long> fees = getFees(cashFlow);
        if (isContainsAnyFee(fees)) {
            payment.setFee(fees.get(FeeType.FEE));
            payment.setProviderFee(fees.get(FeeType.PROVIDER_FEE));
            payment.setExternalFee(fees.get(FeeType.EXTERNAL_FEE));
        }
    }

    private static void fillPayerInfo(Payer payer, Payment payment) {

        payment.setPayerType(TBaseUtil.unionFieldToEnum(payer, PaymentPayerType.class));
        if (payer.isSetPaymentResource()) {
            PaymentResourcePayer paymentResource = payer.getPaymentResource();
            DisposablePaymentResource resource = paymentResource.getResource();
            com.rbkmoney.damsel.domain.PaymentTool paymentTool = resource.getPaymentTool();
            payment.setTool(TBaseUtil.unionFieldToEnum(
                    paymentTool,
                    com.rbkmoney.reporter.domain.enums.PaymentTool.class
            ));
            ContactInfo contactInfo = paymentResource.getContactInfo();
            fillContactInfo(payment, contactInfo);
        } else if (payer.isSetCustomer()) {
            CustomerPayer customer = payer.getCustomer();
            PaymentTool paymentTool = customer.getPaymentTool();
            payment.setTool(TBaseUtil.unionFieldToEnum(
                    paymentTool,
                    com.rbkmoney.reporter.domain.enums.PaymentTool.class
            ));
            ContactInfo contactInfo = customer.getContactInfo();
            fillContactInfo(payment, contactInfo);

        } else if (payer.isSetRecurrent()) {
            RecurrentPayer recurrent = payer.getRecurrent();
            RecurrentParentPayment recurrentParent = recurrent.getRecurrentParent();
            PaymentTool paymentTool = recurrent.getPaymentTool();
            payment.setTool(TBaseUtil.unionFieldToEnum(
                    paymentTool,
                    com.rbkmoney.reporter.domain.enums.PaymentTool.class
            ));
            ContactInfo contactInfo = recurrent.getContactInfo();
            fillContactInfo(payment, contactInfo);
        }
    }

    private static void fillContactInfo(Payment payment, ContactInfo contactInfo) {
        if (contactInfo != null) {
            payment.setPhoneNumber(contactInfo.getPhoneNumber());
            payment.setEmail(contactInfo.getEmail());
        }
    }

    public static void removeNullSymbols(Object object) {
        for (Field field : object.getClass().getDeclaredFields()) {
            removeNullSymbolsFromString(field, object);
        }
    }

    private static void removeNullSymbolsFromString(Field field, Object object) {
        String stringClassName = "String";
        if (stringClassName.equalsIgnoreCase(field.getType().getSimpleName())) {
            try {
                field.setAccessible(true);
                if (field.get(object) != null) {
                    String resultString = field.get(object).toString()
                            .replace("\u0000", "")
                            .replace("\\u0000", "");
                    field.set(object, resultString);
                }
            } catch (IllegalAccessException ex) {
                log.warn("Received exception while removing null characters from strings", ex);
            }
        }
    }

}
