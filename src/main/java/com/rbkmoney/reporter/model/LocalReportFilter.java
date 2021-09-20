package com.rbkmoney.reporter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class LocalReportFilter {

    private String partyId;
    private String shopId;
    private LocalDateTime fromTime;
    private LocalDateTime toTime;

}
