package com.market.coupon;

import java.sql.Timestamp;

/** 회원이 가진 쿠폰 한 장 (쿠폰 정보 + 사용 여부). */
public class UserCoupon {
    private final Coupon coupon;
    private final Timestamp usedAt;

    public UserCoupon(Coupon coupon, Timestamp usedAt) { this.coupon = coupon; this.usedAt = usedAt; }

    public Coupon getCoupon() { return coupon; }
    public Timestamp getUsedAt() { return usedAt; }
    public boolean isUsed() { return usedAt != null; }
    public boolean isAvailable() { return !isUsed() && coupon.isActive() && !coupon.isExpired(); }

    public String getStatus() {
        if (isUsed()) return "사용 완료";
        if (!coupon.isActive()) return "사용 중지";
        if (coupon.isExpired()) return "기한 만료";
        return "사용 가능";
    }
}
