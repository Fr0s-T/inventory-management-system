package Utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * Author: @Frost
 */

public class HashingUtility {

    public static String md5Hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digestBytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : digestBytes) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        return md5Hash(rawPassword).equals(storedHash);
    }
}
