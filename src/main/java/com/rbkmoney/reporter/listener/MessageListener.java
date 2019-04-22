package com.rbkmoney.reporter.listener;

import com.rbkmoney.machinegun.eventsink.MachineEvent;
import org.springframework.kafka.support.Acknowledgment;

public interface MessageListener {

    void listen(MachineEvent machineEvent, Acknowledgment ack);

}
