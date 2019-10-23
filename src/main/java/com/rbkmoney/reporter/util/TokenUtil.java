package com.rbkmoney.reporter.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.reporter.ReportRequest;
import com.rbkmoney.reporter.StatReportRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.List;

@Slf4j
public class TokenUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static String buildToken(ReportRequest request, List<String> reportTypes, String time) {
        return Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(new TokenData(request, reportTypes, time)));
    }

    public static boolean isValid(StatReportRequest statReportRequest) {
        try {
            String sourceToken = statReportRequest.getContinuationToken();
            String sourceTime = extractTime(sourceToken);
            String generatedToken = buildToken(statReportRequest.getRequest(), statReportRequest.getReportTypes(), sourceTime);
            return sourceToken.equals(generatedToken);
        } catch (Exception e) {
            return false;
        }
    }

    @SneakyThrows
    public static String extractTime(String token) {
        return objectMapper.readValue(Base64.getDecoder().decode(token), TokenData.class).getTime();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TokenData {
        private String partyId;
        private String shopId;
        private String fromTime;
        private String toTime;
        private List<String> reportTypes;
        private String time;

        public TokenData(ReportRequest request, List<String> reportTypes, String time) {
            this(request.getPartyId(), request.getShopId(), request.getTimeRange().getFromTime(), request.getTimeRange().getToTime(), reportTypes, time);
        }
    }
}
