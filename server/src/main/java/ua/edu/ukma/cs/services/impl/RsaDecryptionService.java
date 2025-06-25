package ua.edu.ukma.cs.services.impl;

import lombok.SneakyThrows;
import ua.edu.ukma.cs.services.IAsymmetricDecryptionService;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;

public class RsaDecryptionService implements IAsymmetricDecryptionService {

    private static final int KEY_SIZE = 2048;

    private final KeyPair keyPair;
    private final ThreadLocal<Cipher> cipher;

    @SneakyThrows
    public RsaDecryptionService() {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(KEY_SIZE);
        this.keyPair = keyPairGenerator.generateKeyPair();
        this.cipher = ThreadLocal.withInitial(() -> createCipher(keyPair.getPrivate()));
    }

    @SneakyThrows
    private static Cipher createCipher(PrivateKey privateKey) {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        return rsaCipher;
    }

    @Override
    public byte[] getPublicKey() {
        return keyPair.getPublic().getEncoded();
    }

    @Override
    public byte[] decrypt(byte[] data) throws GeneralSecurityException {
        try {
            return cipher.get().doFinal(data);
        }
        catch (GeneralSecurityException ex) {
            cipher.remove();
            throw ex;
        }
    }
}
