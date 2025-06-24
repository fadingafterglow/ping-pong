package ua.edu.ukma.cs.repository.base;

import java.sql.SQLException;

public interface SqlExceptionThrowingFunction<T, R> {
    R apply(T arg) throws SQLException;
}
