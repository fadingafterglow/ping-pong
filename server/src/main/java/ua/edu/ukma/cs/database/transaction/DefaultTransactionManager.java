package ua.edu.ukma.cs.database.transaction;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.database.connection.IConnectionFactory;
import ua.edu.ukma.cs.exception.DataBaseException;

import java.sql.Connection;

@RequiredArgsConstructor
public class DefaultTransactionManager implements TransactionManager {

    private final IConnectionFactory connectionFactory;
    private final ThreadLocal<DefaultTransaction> currentTransaction = new ThreadLocal<>();

    @Override
    public void beginTransaction(boolean readOnly, TransactionIsolation isolation) {
        throwIfStarted();
        try {
            Connection connection = connectionFactory.getConnection();
            connection.setAutoCommit(false);
            connection.setReadOnly(readOnly);
            connection.setTransactionIsolation(isolation.getValue());
            currentTransaction.set(new DefaultTransaction(connection));
        } catch (Exception e) {
            throw new DataBaseException(e);
        }
    }

    @Override
    public boolean isTransactionActive() {
        return currentTransaction.get() != null;
    }

    @Override
    public Transaction currentTransaction() {
        throwIfNotStarted();
        return currentTransaction.get();
    }

    @Override
    public void commitCurrent() {
        throwIfNotStarted();
        try {
            DefaultTransaction transaction = currentTransaction.get();
            transaction.connection.commit();
            transaction.connection.close();
            transaction.connection = null;
            currentTransaction.remove();
        } catch (Exception e) {
            throw new DataBaseException(e);
        }
    }

    @Override
    public void rollbackCurrent() {
        throwIfNotStarted();
        try {
            DefaultTransaction transaction = currentTransaction.get();
            transaction.connection.rollback();
            transaction.connection.close();
            transaction.connection = null;
            currentTransaction.remove();
        } catch (Exception e) {
            throw new DataBaseException(e);
        }
    }

    private void throwIfStarted() {
        if (isTransactionActive())
            throw new DataBaseException("Transaction is already started");
    }

    private void throwIfNotStarted() {
        if (!isTransactionActive())
            throw new DataBaseException("Transaction is not started");
    }
}
