package ua.edu.ukma.cs.repository;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.database.connection.IConnectionFactory;
import ua.edu.ukma.cs.exception.DataBaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@RequiredArgsConstructor
public class TestConnectionFactory implements IConnectionFactory {
    private final String url;
    private final String username;
    private final String password;

    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        }
        catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }
}
