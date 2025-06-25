package ua.edu.ukma.cs.services;

import java.security.GeneralSecurityException;

public interface IAsymmetricDecryptionService {

    byte[] getPublicKey();

    byte[] decrypt(byte[] data) throws GeneralSecurityException;
}
