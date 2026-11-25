package com.market.util;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** PBKDF2 비밀번호 해시. 저장 형식: iterations:salt:hash (Base64) */
public final class Passwords {
    private static final int ITER = 120_000;
    private Passwords() {}

    public static String hash(String pw) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return ITER + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(derive(pw, salt, ITER));
    }

    public static boolean verify(String pw, String stored) {
        if (pw == null || stored == null) return false;
        String[] p = stored.split(":");
        if (p.length != 3) return false;
        try {
            byte[] expected = Base64.getDecoder().decode(p[2]);
            byte[] actual = derive(pw, Base64.getDecoder().decode(p[1]), Integer.parseInt(p[0]));
            return java.security.MessageDigest.isEqual(expected, actual);
        } catch (Exception e) { return false; }
    }

    private static byte[] derive(String pw, byte[] salt, int iter) {
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(new PBEKeySpec(pw.toCharArray(), salt, iter, 256)).getEncoded();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
