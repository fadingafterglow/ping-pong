package ua.edu.ukma.cs.repository.base;

import java.sql.SQLException;

public interface SqlExceptionThrowingSupplier<T> {
    T supply() throws SQLException;
}
