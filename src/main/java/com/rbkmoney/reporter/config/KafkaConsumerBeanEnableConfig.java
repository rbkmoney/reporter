package com.rbkmoney.reporter.config;

import com.rbkmoney.reporter.converter.SourceEventParser;
import com.rbkmoney.reporter.handle.machineevent.impl.PaymentMachineEventHandler;
import com.rbkmoney.reporter.handle.machineevent.impl.PayoutMachineEventHandler;
import com.rbkmoney.reporter.listener.MessageListener;
import com.rbkmoney.reporter.listener.impl.PaymentEventsMessageListenerImpl;
import com.rbkmoney.reporter.listener.impl.PayoutEventsMessageListenerImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConsumerBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "kafka.processing.payment.enabled", havingValue = "true")
    public MessageListener paymentEventsKafkaListener(SourceEventParser sourceEventParser,
                                                      PaymentMachineEventHandler handler) {
        return new PaymentEventsMessageListenerImpl(sourceEventParser, handler);
    }

    @Bean
    @ConditionalOnProperty(value = "kafka.processing.payout.enabled", havingValue = "true")
    public MessageListener payoutEventsKafkaListener(SourceEventParser sourceEventParser,
                                                     PayoutMachineEventHandler handler) {
        return new PayoutEventsMessageListenerImpl(sourceEventParser, handler);
    }
}
