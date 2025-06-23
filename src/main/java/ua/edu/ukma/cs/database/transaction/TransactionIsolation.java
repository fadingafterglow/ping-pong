package ua.edu.ukma.cs.database.transaction;

import lombok.Getter;

import java.sql.Connection;

@Getter
public enum TransactionIsolation {
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    private final int value;

    TransactionIsolation(int value) {
        this.value = value;
    }

    public static TransactionIsolation valueOf(int value) {
        return switch (value) {
            case Connection.TRANSACTION_READ_UNCOMMITTED -> READ_UNCOMMITTED;
            case Connection.TRANSACTION_READ_COMMITTED -> READ_COMMITTED;
            case Connection.TRANSACTION_REPEATABLE_READ -> REPEATABLE_READ;
            case Connection.TRANSACTION_SERIALIZABLE -> SERIALIZABLE;
            default -> throw new IllegalArgumentException("Unknown transaction isolation value: " + value);
        };
    }
}
