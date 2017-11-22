package com.rbkmoney.reporter.dsl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Collection;

/**
 * Created by tolkonepiu on 14/07/2017.
 */
public class ShopAccountingQuery {

    @JsonProperty("from_time")
    Instant fromTime;

    @JsonProperty("to_time")
    Instant toTime;

    @JsonProperty("shop_category_ids")
    Collection<Integer> shopCategoryIds;

    public Instant getFromTime() {
        return fromTime;
    }

    public void setFromTime(Instant fromTime) {
        this.fromTime = fromTime;
    }

    public Instant getToTime() {
        return toTime;
    }

    public void setToTime(Instant toTime) {
        this.toTime = toTime;
    }

    public Collection<Integer> getShopCategoryIds() {
        return shopCategoryIds;
    }

    public void setShopCategoryIds(Collection<Integer> shopCategoryIds) {
        this.shopCategoryIds = shopCategoryIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopAccountingQuery that = (ShopAccountingQuery) o;

        if (fromTime != null ? !fromTime.equals(that.fromTime) : that.fromTime != null) return false;
        if (toTime != null ? !toTime.equals(that.toTime) : that.toTime != null) return false;
        return shopCategoryIds != null ? shopCategoryIds.equals(that.shopCategoryIds) : that.shopCategoryIds == null;
    }

    @Override
    public int hashCode() {
        int result = fromTime != null ? fromTime.hashCode() : 0;
        result = 31 * result + (toTime != null ? toTime.hashCode() : 0);
        result = 31 * result + (shopCategoryIds != null ? shopCategoryIds.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ShopAccountingQuery{" +
                "fromTime=" + fromTime +
                ", toTime=" + toTime +
                ", shopCategoryIds=" + shopCategoryIds +
                '}';
    }
}
