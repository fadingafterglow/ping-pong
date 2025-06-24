package ua.edu.ukma.cs.services;

import ua.edu.ukma.cs.exception.ValidationException;

public interface ISymmetricEncryptionService {

    void validateKey(byte[] key) throws ValidationException;

    byte[] encrypt(byte[] data, byte[] key);

    byte[] decrypt(byte[] data, byte[] key);
}
