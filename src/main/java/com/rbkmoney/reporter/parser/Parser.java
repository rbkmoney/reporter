package com.rbkmoney.reporter.parser;

public interface Parser<F, T> {

    T parse(F data);
}
