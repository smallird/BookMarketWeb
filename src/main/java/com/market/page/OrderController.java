package com.market.page;

import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import com.market.cart.*;
import com.market.member.Auth;

@WebServlet("/order.do")
public class OrderController extends HttpServlet {
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession s = req.getSession();
        String ctx = req.getContextPath();
        String userId = (String) s.getAttribute("userId");
        if (userId == null) { res.sendRedirect(ctx + "/login.jsp"); return; }

        List<CartItem> cart = (List<CartItem>) s.getAttribute("cart");
        if (cart == null || cart.isEmpty()) { res.sendRedirect(ctx + "/cart.jsp"); return; }

        try {
            // 금액·할인은 화면에서 넘어온 값을 믿지 않고 서버에서 다시 계산합니다.
            Order order = OrderDao.place(userId, req.getParameter("name"), req.getParameter("phone"),
                    req.getParameter("address"), cart, req.getParameter("couponCode"));
            s.setAttribute("lastOrder", order);
            cart.clear();
            try { CartDao.save(userId, cart); } catch (Exception e) { log("cart clear failed", e); }
            res.sendRedirect(ctx + "/orderComplete.jsp");
        } catch (OrderDao.CouponException e) {
            Auth.flash(s, "danger", e.getMessage());
            res.sendRedirect(ctx + "/order.jsp");
        } catch (Exception e) {
            log("order failed", e);
            Auth.flash(s, "danger", "주문을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.");
            res.sendRedirect(ctx + "/order.jsp");
        }
    }
}
