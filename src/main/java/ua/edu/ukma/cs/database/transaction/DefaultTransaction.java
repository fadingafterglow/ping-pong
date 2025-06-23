package ua.edu.ukma.cs.database.transaction;

import ua.edu.ukma.cs.exception.DataBaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class DefaultTransaction implements Transaction {

    Connection connection;

    DefaultTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Statement createStatement() {
        try {
            return connection.createStatement();
        }
        catch (NullPointerException e) {
            throw new DataBaseException("Transaction is closed");
        }
        catch (Exception e) {
            throw new DataBaseException(e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql) {
        return prepareStatement(sql, false);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, boolean returnGeneratedKeys) {
        try {
            return connection.prepareStatement(sql, returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
        }
        catch (NullPointerException e) {
            throw new DataBaseException("Transaction is closed");
        }
        catch (Exception e) {
            throw new DataBaseException(e);
        }
    }

    @Override
    public boolean isReadOnly() {
        try {
            return connection.isReadOnly();
        }
        catch (NullPointerException e) {
            throw new DataBaseException("Transaction is closed");
        }
        catch (Exception e) {
            throw new DataBaseException(e);
        }
    }

    @Override
    public TransactionIsolation getIsolation() {
        try {
            return TransactionIsolation.valueOf(connection.getTransactionIsolation());
        }
        catch (NullPointerException e) {
            throw new DataBaseException("Transaction is closed");
        }
        catch (Exception e) {
            throw new DataBaseException(e);
        }
    }
}
