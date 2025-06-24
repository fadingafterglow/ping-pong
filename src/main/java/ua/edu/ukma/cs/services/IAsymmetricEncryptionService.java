package ua.edu.ukma.cs.services;

import java.security.GeneralSecurityException;

public interface IAsymmetricEncryptionService {

    byte[] getPublicKey();

    byte[] decrypt(byte[] data) throws GeneralSecurityException;
}
