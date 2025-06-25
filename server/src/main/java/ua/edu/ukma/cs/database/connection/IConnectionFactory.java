package ua.edu.ukma.cs.database.connection;

import java.sql.Connection;

public interface IConnectionFactory {

    Connection getConnection();
}
