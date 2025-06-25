package ua.edu.ukma.cs.encryption;

import lombok.SneakyThrows;
import ua.edu.ukma.cs.exception.ValidationException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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

    public byte[] generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();
        return secretKey.getEncoded();
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
