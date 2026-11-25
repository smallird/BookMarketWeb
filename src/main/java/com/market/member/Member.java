package com.market.member;

/** DB members 테이블의 한 행. */
public class Member {
    private String id, name, phone, address, role, kakaoId, pwHash, genrePreferences, interestKeywords;

    public Member(String id, String name, String phone, String address, String role, String kakaoId, String pwHash) {
        this(id,name,phone,address,role,kakaoId,pwHash,"","");
    }
    public Member(String id, String name, String phone, String address, String role, String kakaoId,
                  String pwHash, String genrePreferences, String interestKeywords) {
        this.id=id; this.name=name; this.phone=phone; this.address=address; this.role=role;
        this.kakaoId=kakaoId; this.pwHash=pwHash;
        this.genrePreferences=genrePreferences==null?"":genrePreferences;
        this.interestKeywords=interestKeywords==null?"":interestKeywords;
    }
    public String getId(){return id;} public String getName(){return name;}
    public String getPhone(){return phone;} public String getAddress(){return address;}
    public String getRole(){return role;} public String getKakaoId(){return kakaoId;}
    String getPwHash(){return pwHash;}
    public String getGenrePreferences(){return genrePreferences;}
    public String getInterestKeywords(){return interestKeywords;}
    public boolean isAdmin(){return "관리자".equals(role);}
}
