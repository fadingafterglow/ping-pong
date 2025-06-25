package ua.edu.ukma.cs.repository.base;

import ua.edu.ukma.cs.database.context.PersistenceContext;
import ua.edu.ukma.cs.database.transaction.TransactionManager;
import ua.edu.ukma.cs.exception.DataBaseException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseRepository<TEntity> {

    protected final TransactionManager transactionManager;

    public BaseRepository() {
        this.transactionManager = PersistenceContext.getInstance().getTransactionManager();
    }

    protected <R> R withStatementInCurrentTransaction(String sql, SqlExceptionThrowingFunction<PreparedStatement, R> function) {
        return withStatementInCurrentTransaction(sql, false, function);
    }

    protected <R> R withStatementInCurrentTransaction(String sql, boolean returnGeneratedKeys, SqlExceptionThrowingFunction<PreparedStatement, R> function) {
        return withSqlExceptionHandling(() -> {
            try (PreparedStatement statement = transactionManager.currentTransaction().prepareStatement(sql, returnGeneratedKeys)) {
                return function.apply(statement);
            }
        });
    }

    protected <R> R withSqlExceptionHandling(SqlExceptionThrowingSupplier<R> supplier) {
        try {
            return supplier.supply();
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    protected int readGeneratedKey(PreparedStatement statement) throws SQLException {
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next())
                return generatedKeys.getInt(1);
            else
                throw new DataBaseException("Cannot create entity");
        }
    }

    protected Optional<TEntity> queryOne(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return Optional.of(readEntityFromResultSet(resultSet));
            }
            return Optional.empty();
        }
    }

    protected List<TEntity> queryAll(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            List<TEntity> entities = new ArrayList<>();
            while (resultSet.next()) {
                entities.add(readEntityFromResultSet(resultSet));
            }
            return entities;
        }
    }

    protected long queryCount(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next())
                return resultSet.getLong(1);
            return 0;
        }
    }

    protected abstract TEntity readEntityFromResultSet(ResultSet resultSet) throws SQLException;
}
