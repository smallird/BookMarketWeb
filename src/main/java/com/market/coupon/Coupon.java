package com.market.coupon;

import java.time.LocalDate;

public class Coupon {
    public static final String PERCENT = "PERCENT", AMOUNT = "AMOUNT";

    private final String code, name, type;
    private final int value, minOrder, maxDiscount;
    private final LocalDate expiresAt;       // null 이면 기한 없음
    private final boolean active, autoIssue;

    public Coupon(String code, String name, String type, int value, int minOrder, int maxDiscount,
                  LocalDate expiresAt, boolean active, boolean autoIssue) {
        this.code = code; this.name = name; this.type = type; this.value = value;
        this.minOrder = minOrder; this.maxDiscount = maxDiscount;
        this.expiresAt = expiresAt; this.active = active; this.autoIssue = autoIssue;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getValue() { return value; }
    public int getMinOrder() { return minOrder; }
    public int getMaxDiscount() { return maxDiscount; }
    public LocalDate getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
    public boolean isAutoIssue() { return autoIssue; }

    public boolean isExpired() { return expiresAt != null && expiresAt.isBefore(LocalDate.now()); }

    /** 주문 금액(subtotal)에 적용했을 때 할인액. 최소 주문금액 미만이면 0. */
    public int discountFor(int subtotal) {
        if (subtotal < minOrder) return 0;
        long d = PERCENT.equals(type) ? (long) subtotal * value / 100 : value;
        if (PERCENT.equals(type) && maxDiscount > 0) d = Math.min(d, maxDiscount);
        return (int) Math.max(0, Math.min(d, subtotal));
    }

    /** 화면에 보여줄 혜택 문구 */
    public String getBenefit() {
        if (PERCENT.equals(type)) return value + "% 할인" + (maxDiscount > 0 ? " (최대 " + String.format("%,d", maxDiscount) + "원)" : "");
        return String.format("%,d", value) + "원 할인";
    }

    public String getCondition() {
        return minOrder > 0 ? String.format("%,d", minOrder) + "원 이상 주문 시" : "최소 주문금액 없음";
    }

    public String getExpiryText() { return expiresAt == null ? "기한 없음" : expiresAt + "까지"; }
}
