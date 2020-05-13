package com.rbkmoney.reporter.util.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayoutSummary {

    @JsonProperty("payout_summary_items")
    private List<PayoutSummaryItem> payoutSummaryItems;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"amount", "fee", "currency", "from_time", "to_time", "operation_type", "count"})
    public static class PayoutSummaryItem {

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");

        private Long amount;
        private Long fee;
        private CurrencyRef currency;
        @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
        @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
        @JsonProperty("from_time")
        private LocalDateTime fromTime;
        @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
        @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
        @JsonProperty("to_time")
        private LocalDateTime toTime;
        @JsonProperty("operation_type")
        private OperationType operationType;
        private Integer count;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CurrencyRef {

            @JsonProperty("symbolic_code")
            private String symbolicCode;

        }

        public enum OperationType {

            @JsonProperty("payment")
            PAYMENT,
            @JsonProperty("refund")
            REFUND,
            @JsonProperty("adjustment")
            ADJUSTMENT

        }

        public static class CustomLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                gen.writeString(value.format(formatter));
            }
        }

        public static class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                return LocalDateTime.parse(p.getText(), formatter);
            }
        }
    }
}
