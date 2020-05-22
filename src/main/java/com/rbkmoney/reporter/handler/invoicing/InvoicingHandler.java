package com.rbkmoney.reporter.handler.invoicing;

import com.rbkmoney.damsel.payment_processing.EventRange;
import com.rbkmoney.damsel.payment_processing.ServiceUser;
import com.rbkmoney.damsel.payment_processing.UserInfo;
import com.rbkmoney.damsel.payment_processing.UserType;

public abstract class InvoicingHandler {

    private static final String USER_INFO_ID = "admin";

    public static final UserInfo USER_INFO = new UserInfo()
            .setId(USER_INFO_ID)
            .setType(UserType.service_user(new ServiceUser()));

    public EventRange getEventRange(int sequenceId) {
        return new EventRange().setLimit(sequenceId);
    }

}
