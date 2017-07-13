package com.rbkmoney.reporter;

import com.rbkmoney.geck.common.util.TypeUtil;

import java.util.Date;

/**
 * Created by tolkonepiu on 13/07/2017.
 */
public class DateTimeParser {

    public Date parse(String isoDateString) {
        return Date.from(TypeUtil.stringToInstant(isoDateString));
    }

}
