package com.market.cart;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.market.bookitem.Book;
import com.market.coupon.Coupon;
import com.market.coupon.CouponDao;
import com.market.util.Db;

public final class OrderDao {
    private OrderDao() {}

    /** 쿠폰을 쓸 수 없을 때 던지는 예외 (메시지는 사용자에게 그대로 보여줍니다) */
    public static class CouponException extends Exception {
        public CouponException(String m) { super(m); }
    }

    /** 주문 저장 + 쿠폰 사용 처리를 한 트랜잭션으로 처리. couponCode 는 비어 있을 수 있음. */
    public static Order place(String userId, String name, String phone, String address,
                              List<CartItem> cart, String couponCode) throws SQLException, CouponException {
        int subtotal = 0;
        for (CartItem ci : cart) subtotal += ci.getTotalPrice();

        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                Coupon k = null; int discount = 0;
                if (couponCode != null && !couponCode.isBlank()) {
                    k = CouponDao.findUsable(c, userId, couponCode.trim());
                    if (k == null) throw new CouponException("사용할 수 없는 쿠폰입니다. 이미 사용했거나 기한이 지났을 수 있어요.");
                    if (subtotal < k.getMinOrder())
                        throw new CouponException("이 쿠폰은 " + String.format("%,d", k.getMinOrder()) + "원 이상 주문할 때 쓸 수 있어요.");
                    discount = k.discountFor(subtotal);
                }

                Date now = new Date();
                String orderNo = nextOrderNo(c, now);
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO orders (order_no,user_id,ordered_at,name,phone,address,subtotal,discount,total,coupon_code,coupon_name) VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
                    ps.setString(1, orderNo); ps.setString(2, userId); ps.setTimestamp(3, new Timestamp(now.getTime()));
                    ps.setString(4, name); ps.setString(5, phone); ps.setString(6, address);
                    ps.setInt(7, subtotal); ps.setInt(8, discount); ps.setInt(9, subtotal - discount);
                    ps.setString(10, k == null ? null : k.getCode()); ps.setString(11, k == null ? null : k.getName());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO order_items (order_no,book_id,book_name,unit_price,quantity) VALUES (?,?,?,?,?)")) {
                    for (CartItem ci : cart) {
                        ps.setString(1, orderNo); ps.setString(2, ci.getBookID()); ps.setString(3, ci.getItemBook().getName());
                        ps.setInt(4, ci.getItemBook().getUnitPrice()); ps.setInt(5, ci.getQuantity());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                if (k != null && !CouponDao.markUsed(c, userId, k.getCode(), orderNo))
                    throw new CouponException("이미 사용한 쿠폰입니다.");
                c.commit();

                Order o = new Order();
                o.setOrderNo(orderNo); o.setUserId(userId); o.setName(name); o.setPhone(phone); o.setAddress(address);
                o.setSubtotal(subtotal); o.setDiscount(discount); o.setTotal(subtotal - discount);
                if (k != null) { o.setCouponCode(k.getCode()); o.setCouponName(k.getName()); }
                return o;
            } catch (SQLException | CouponException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    /** ORD-20261125-001 형식. 오늘 이미 들어온 주문 수 + 1 (서버를 다시 켜도 이어집니다) */
    private static synchronized String nextOrderNo(Connection c, Date now) throws SQLException {
        String prefix = "ORD-" + new SimpleDateFormat("yyyyMMdd").format(now) + "-";
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM orders WHERE order_no LIKE ?")) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return prefix + String.format("%03d", rs.getInt(1) + 1); }
        }
    }

    /** userId 가 null 이면 전체(관리자용), 아니면 해당 회원의 주문. 최신순. */
    public static List<Order> list(String userId) throws SQLException {
        Map<String, Order> map = new LinkedHashMap<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try (Connection c = Db.get()) {
            String sql = "SELECT order_no,user_id,ordered_at,name,phone,address,subtotal,discount,total,coupon_code,coupon_name FROM orders"
                    + (userId == null ? "" : " WHERE user_id=?") + " ORDER BY order_no DESC";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (userId != null) ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Order o = new Order();
                        o.setOrderNo(rs.getString(1)); o.setUserId(rs.getString(2)); o.setDate(fmt.format(rs.getTimestamp(3)));
                        o.setName(rs.getString(4)); o.setPhone(rs.getString(5)); o.setAddress(rs.getString(6));
                        o.setSubtotal(rs.getInt(7)); o.setDiscount(rs.getInt(8)); o.setTotal(rs.getInt(9));
                        o.setCouponCode(rs.getString(10)); o.setCouponName(rs.getString(11));
                        map.put(o.getOrderNo(), o);
                    }
                }
            }
            if (map.isEmpty()) return new ArrayList<>();
            String isql = "SELECT i.order_no,i.book_id,i.book_name,i.unit_price,i.quantity FROM order_items i"
                    + (userId == null ? "" : " JOIN orders o ON o.order_no=i.order_no WHERE o.user_id=?") + " ORDER BY i.order_no";
            try (PreparedStatement ps = c.prepareStatement(isql)) {
                if (userId != null) ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Order o = map.get(rs.getString(1));
                        if (o == null) continue;
                        CartItem ci = new CartItem(new Book(rs.getString(2), rs.getString(3), rs.getInt(4)));
                        ci.setQuantity(rs.getInt(5));
                        o.getItems().add(ci);
                    }
                }
            }
        }
        return new ArrayList<>(map.values());
    }
}
