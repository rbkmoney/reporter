package com.rbkmoney.reporter.dsl;

/**
 * Created by tolkonepiu on 10/07/2017.
 */
public class Query {

    private PaymentQuery payments;

    public PaymentQuery getPayments() {
        return payments;
    }

    public void setPayments(PaymentQuery payments) {
        this.payments = payments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Query query = (Query) o;

        return payments != null ? payments.equals(query.payments) : query.payments == null;
    }

    @Override
    public int hashCode() {
        return payments != null ? payments.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Query{" +
                "payments=" + payments +
                '}';
    }
}
