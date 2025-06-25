package ua.edu.ukma.cs.security;

import lombok.SneakyThrows;

import java.security.MessageDigest;

public class PasswordHashGenerator {
    public boolean check(String password, String hash) {
        return hash(password).equals(hash);
    }

    @SneakyThrows
    public String hash(String password) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
