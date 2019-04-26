package com.rbkmoney.reporter.listener;

import com.rbkmoney.machinegun.eventsink.SinkEvent;
import org.springframework.kafka.support.Acknowledgment;

public interface MessageListener {

    void listen(SinkEvent event, Acknowledgment ack);

}
