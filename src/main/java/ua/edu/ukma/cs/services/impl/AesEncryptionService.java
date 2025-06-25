package ua.edu.ukma.cs.services.impl;

import lombok.SneakyThrows;
import ua.edu.ukma.cs.exception.ValidationException;
import ua.edu.ukma.cs.services.ISymmetricEncryptionService;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;

public class AesEncryptionService implements ISymmetricEncryptionService {

    private final ThreadLocal<Cipher> cipher;

    public AesEncryptionService() {
        this.cipher = ThreadLocal.withInitial(AesEncryptionService::createCipher);
    }

    @SneakyThrows
    private static Cipher createCipher() {
        return Cipher.getInstance("AES/ECB/PKCS5Padding");
    }

    @Override
    public void validateKey(byte[] key) throws ValidationException {
        try {
            cipher.get().init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        } catch (InvalidKeyException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Override
    @SneakyThrows
    public byte[] encrypt(byte[] data, byte[] key) {
        Cipher c = cipher.get();
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        return c.doFinal(data);
    }

    @Override
    @SneakyThrows
    public byte[] decrypt(byte[] data, byte[] key) {
        Cipher c = cipher.get();
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
        return c.doFinal(data);
    }
}
