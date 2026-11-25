package com.market.cart;

import java.util.*;

/** 주문 한 건 (orders + order_items). */
public class Order {
    private String orderNo, userId, date, name, phone, address, couponCode, couponName;
    private int subtotal, discount, total;
    private List<CartItem> items = new ArrayList<>();

    public String getOrderNo() { return orderNo; }
    public String getUserId() { return userId; }
    public String getDate() { return date; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCouponCode() { return couponCode; }
    public String getCouponName() { return couponName; }
    public int getSubtotal() { return subtotal; }
    public int getDiscount() { return discount; }
    public int getTotal() { return total; }
    public List<CartItem> getItems() { return items; }

    public void setOrderNo(String v) { orderNo = v; }
    public void setUserId(String v) { userId = v; }
    public void setDate(String v) { date = v; }
    public void setName(String v) { name = v; }
    public void setPhone(String v) { phone = v; }
    public void setAddress(String v) { address = v; }
    public void setCouponCode(String v) { couponCode = v; }
    public void setCouponName(String v) { couponName = v; }
    public void setSubtotal(int v) { subtotal = v; }
    public void setDiscount(int v) { discount = v; }
    public void setTotal(int v) { total = v; }
    public void setItems(List<CartItem> v) { items = v; }
}
