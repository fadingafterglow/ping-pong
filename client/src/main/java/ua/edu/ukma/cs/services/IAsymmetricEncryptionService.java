package ua.edu.ukma.cs.services;

public interface IAsymmetricEncryptionService {
    byte[] encrypt(byte[] data, byte[] publicKey) throws Exception;
} 