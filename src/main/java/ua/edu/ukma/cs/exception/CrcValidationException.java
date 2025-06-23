package ua.edu.ukma.cs.exception;

public class CrcValidationException extends ValidationException {

    public CrcValidationException() {
        super("Invalid CRC");
    }
}
