package com.rbkmoney.reporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.rbkmoney.reporter.dsl.StatisticDsl;
import org.junit.Ignore;
import org.junit.Test;

import static io.github.benas.randombeans.api.EnhancedRandom.random;

/**
 * Created by tolkonepiu on 10/07/2017.
 */
@Ignore
public class DslTest {

    @Test
    public void magistaDslTest() throws JsonProcessingException {
        StatisticDsl dsl = random(StatisticDsl.class);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        System.out.println(objectMapper.writeValueAsString(dsl));
    }

}
