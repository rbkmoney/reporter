package com.rbkmoney.reporter.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rbkmoney.reporter.batch.impl.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.batch.impl.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<InvoiceUniqueBatchKeyImpl, PartyData> partyDataCache(@Value("${cache.invoice.size}") int cacheSize) {
        return Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .build();
    }

    @Bean
    public Cache<PaymentInvoiceUniqueBatchKey, PaymentPartyData> paymentPartyDataCache(@Value("${cache.payment.size}") int cacheSize) {
        return Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .build();
    }
}
