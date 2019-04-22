package com.rbkmoney.reporter.kafka;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.reporter.parser.impl.PaymentMachineEventParser;
import com.rbkmoney.reporter.serialization.impl.MachineEventSerializerImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@Slf4j
public class ConsumerTests extends AbstractAppKafkaTests {

    @MockBean
    private PaymentMachineEventParser parser;

    @Value("${kafka.processing.payment.topic}")
    public String topic;

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Test
    public void paymentProcessingKafkaListenerTest() throws InterruptedException {
        when(parser.parse(any())).thenReturn(EventPayload.invoice_changes(Collections.emptyList()));

        Producer<String, SinkEvent> producer = createProducer();

        MachineEvent machineEvent = new MachineEvent();
        machineEvent.setSourceNs("expected_ns");
        machineEvent.setSourceId(generateString());
        machineEvent.setEventId(generateLong());
        machineEvent.setCreatedAt(generateDate());
        machineEvent.setData(com.rbkmoney.machinegun.msgpack.Value.bin(new byte[0]));

        ProducerRecord<String, SinkEvent> producerRecord = new ProducerRecord<>(topic, null, SinkEvent.event(machineEvent));

        try {
            producer.send(producerRecord).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("ConsumerTests paymentProcessingTest e: ", e);
        }
        producer.close();

        TimeUnit.SECONDS.sleep(1);

        verify(parser, times(1)).parse(any());
    }

    public Producer<String, SinkEvent> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "client_id");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MachineEventSerializerImpl.class.getName());
        return new KafkaProducer<>(props);
    }
}
