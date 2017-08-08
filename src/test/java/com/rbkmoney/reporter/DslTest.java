package com.rbkmoney.reporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.reporter.service.StatisticService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Created by tolkonepiu on 10/07/2017.
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = RANDOM_PORT)
public class DslTest {
//
//    @Autowired
//    StatisticService statisticService;

    @Test
    public void magistaDslTest() throws JsonProcessingException {
        Instant fromTime = LocalDateTime.of(2016, 01, 01, 00, 00).toInstant(ZoneOffset.UTC);
        Instant toTime = LocalDateTime.of(2016, 12, 31, 11, 59).toInstant(ZoneOffset.UTC);
        System.out.println(String.format("%s %s", fromTime, toTime));
//        System.out.println(statisticService.getShopAccountings(fromTime, toTime));
    }

}
