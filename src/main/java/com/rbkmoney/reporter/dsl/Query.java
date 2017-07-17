package com.rbkmoney.reporter.dsl;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by tolkonepiu on 10/07/2017.
 */
public class Query {

    @JsonProperty("shop_accounting_report")
    private ShopAccountingQuery shopAccountingQuery;

    public ShopAccountingQuery getShopAccountingQuery() {
        return shopAccountingQuery;
    }

    public void setShopAccountingQuery(ShopAccountingQuery shopAccountingQuery) {
        this.shopAccountingQuery = shopAccountingQuery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Query query = (Query) o;

        return shopAccountingQuery != null ? shopAccountingQuery.equals(query.shopAccountingQuery) : query.shopAccountingQuery == null;
    }

    @Override
    public int hashCode() {
        return shopAccountingQuery != null ? shopAccountingQuery.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Query{" +
                "shopAccountingQuery=" + shopAccountingQuery +
                '}';
    }
}
