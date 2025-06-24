package ua.edu.ukma.cs.repository.base;

import ua.edu.ukma.cs.database.context.PersistenceContext;
import ua.edu.ukma.cs.database.transaction.TransactionManager;
import ua.edu.ukma.cs.exception.DataBaseException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseRepository {

    protected final TransactionManager transactionManager;

    public BaseRepository() {
        this.transactionManager = PersistenceContext.getInstance().getTransactionManager();
    }

    public <R> R withStatementInCurrentTransaction(String sql, boolean returnGeneratedKeys, SqlExceptionThrowingFunction<PreparedStatement, R> function) {
        return withSqlExceptionHandling(() -> {
            try (PreparedStatement statement = transactionManager.currentTransaction().prepareStatement(sql, returnGeneratedKeys)) {
                return function.apply(statement);
            }
        });
    }

    public <R> R withSqlExceptionHandling(SqlExceptionThrowingSupplier<R> supplier) {
        try {
            return supplier.supply();
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    public int readGeneratedKey(PreparedStatement statement) {
        return withSqlExceptionHandling(() -> {
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next())
                    return generatedKeys.getInt(1);
                else
                    throw new DataBaseException("Cannot create entity");
            }
        });
    }
}
