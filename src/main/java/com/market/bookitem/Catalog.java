package com.market.bookitem;

import java.sql.*;
import java.util.*;
import jakarta.servlet.ServletContext;
import com.market.util.Db;

/** 도서 목록 (DB books 테이블). 기존 컨트롤러가 쓰던 메서드 이름을 그대로 유지합니다. */
public class Catalog {
    private static final String COLS = "book_id,name,unit_price,author,description,category,release_date";

    /** 예전에는 book.txt 를 읽었지만 지금은 서버 시작 시 AppInit 이 DB 에 넣습니다. 호환을 위해 남겨 둔 빈 메서드. */
    public static void init(ServletContext context) { }

    private static Book map(ResultSet rs) throws SQLException {
        return new Book(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7));
    }

    public static List<Book> all() {
        List<Book> list = new ArrayList<>();
        try (Connection c = Db.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT " + COLS + " FROM books ORDER BY sort_no, book_id")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { throw new IllegalStateException("도서 목록을 불러오지 못했습니다.", e); }
        return list;
    }

    public static Book find(String id) {
        if (id == null) return null;
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM books WHERE book_id=?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new IllegalStateException("도서를 불러오지 못했습니다.", e); }
    }

    /** 검색 API에서 선택한 책을 DB에 저장합니다. 이미 등록된 ISBN이면 기존 도서를 유지합니다. */
    public static void addIfAbsent(Book b) {
        String sql = "INSERT INTO books (" + COLS + ",sort_no) VALUES (?,?,?,?,?,?,?,?) ON CONFLICT (book_id) DO NOTHING";
        try (Connection c=Db.get(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,b.getBookId()); ps.setString(2,b.getName()); ps.setInt(3,b.getUnitPrice());
            ps.setString(4,b.getAuthor()); ps.setString(5,b.getDescription()); ps.setString(6,b.getCategory());
            ps.setString(7,b.getReleaseDate()); ps.setLong(8,System.currentTimeMillis()); ps.executeUpdate();
        } catch(SQLException e) { throw new IllegalStateException("검색 도서를 저장하지 못했습니다.",e); }
    }

    /** 회원이 선택한 장르와 도서 카테고리가 일치하는 책을 우선 추천합니다. */
    public static List<Book> recommend(String preferences, int limit) {
        return recommend(preferences, "", limit);
    }

    public static List<Book> recommend(String preferences, String keywords, int limit) {
        List<Book> all=all(), result=new ArrayList<>();
        if ((preferences==null || preferences.isBlank()) && (keywords==null || keywords.isBlank()))
            return all.subList(0,Math.min(limit,all.size()));
        String[] genres=preferences==null?new String[0]:preferences.split(",");
        String[] words=keywords==null?new String[0]:keywords.split("[,，]");
        for(Book b:all) {
            String category=b.getCategory()==null?"":b.getCategory().toLowerCase(Locale.ROOT);
            String searchable=((b.getName()==null?"":b.getName())+" "+(b.getAuthor()==null?"":b.getAuthor())
                    +" "+(b.getDescription()==null?"":b.getDescription())).toLowerCase(Locale.ROOT);
            boolean matched=false;
            for(String g:genres) {
                String key=g.trim().toLowerCase(Locale.ROOT);
                if(key.equals("it·컴퓨터")) key="it";
                if(key.equals("경제·경영")) key="경제";
                if(key.equals("취미·실용")) key="취미";
                if(!key.isBlank() && category.contains(key)) { matched=true; break; }
            }
            if(!matched) for(String word:words) {
                String key=word.trim().toLowerCase(Locale.ROOT);
                if(!key.isBlank() && searchable.contains(key)) { matched=true; break; }
            }
            if(matched) result.add(b);
            if(result.size()>=limit) break;
        }
        return result;
    }

    /** 같은 ID 가 있으면 예외 (호출하는 쪽에서 처리) */
    public static void add(Book b) {
        String sql = "INSERT INTO books (" + COLS + ",sort_no) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.getBookId()); ps.setString(2, b.getName()); ps.setInt(3, b.getUnitPrice());
            ps.setString(4, b.getAuthor()); ps.setString(5, b.getDescription());
            ps.setString(6, b.getCategory()); ps.setString(7, b.getReleaseDate());
            ps.setLong(8, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("도서를 등록하지 못했습니다.", e); }
    }

    public static void remove(String id) {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM books WHERE book_id=?")) {
            ps.setString(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("도서를 삭제하지 못했습니다.", e); }
    }
}
