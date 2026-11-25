package com.market.cart;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import com.market.bookitem.Book;
import com.market.util.Db;

/** 회원별 장바구니를 DB(cart_items)에 저장합니다. 화면은 계속 세션의 "cart" 목록을 읽습니다. */
public final class CartDao {
    private CartDao() {}

    /** DB 에 저장된 장바구니. 그 사이 삭제된 도서는 빠집니다. */
    public static List<CartItem> load(String userId) throws SQLException {
        List<CartItem> list = new ArrayList<>();
        String sql = "SELECT c.quantity,b.book_id,b.name,b.unit_price,b.author,b.description,b.category,b.release_date"
                + " FROM cart_items c JOIN books b ON b.book_id=c.book_id WHERE c.user_id=? ORDER BY c.pos";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem ci = new CartItem(new Book(rs.getString(2), rs.getString(3), rs.getInt(4),
                            rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8)));
                    ci.setQuantity(rs.getInt(1));
                    list.add(ci);
                }
            }
        }
        return list;
    }

    /** 현재 장바구니 내용으로 DB 를 통째로 교체합니다. (한 트랜잭션) */
    public static void save(String userId, List<CartItem> cart) throws SQLException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement del = c.prepareStatement("DELETE FROM cart_items WHERE user_id=?")) {
                    del.setString(1, userId); del.executeUpdate();
                }
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO cart_items (user_id,book_id,quantity,pos) VALUES (?,?,?,?)")) {
                    int pos = 0;
                    for (CartItem ci : cart) {
                        ins.setString(1, userId); ins.setString(2, ci.getBookID());
                        ins.setInt(3, ci.getQuantity()); ins.setInt(4, pos++);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }

    /** 로그인 직후: 로그인 전에 담아 둔 장바구니를 DB 장바구니에 합쳐 세션에 올리고 저장합니다. */
    @SuppressWarnings("unchecked")
    public static void mergeOnLogin(HttpSession s, String userId) throws SQLException {
        List<CartItem> merged = load(userId);
        List<CartItem> guest = (List<CartItem>) s.getAttribute("cart");
        if (guest != null) {
            for (CartItem g : guest) {
                CartItem same = null;
                for (CartItem m : merged) if (m.getBookID().equals(g.getBookID())) { same = m; break; }
                if (same != null) same.setQuantity(same.getQuantity() + g.getQuantity());
                else merged.add(g);
            }
        }
        s.setAttribute("cart", merged);
        if (guest != null && !guest.isEmpty()) save(userId, merged);
    }
}
