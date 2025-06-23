package ua.edu.ukma.cs.database.transaction;

import java.sql.PreparedStatement;
import java.sql.Statement;

public interface Transaction {

    Statement createStatement();

    PreparedStatement prepareStatement(String sql);

    PreparedStatement prepareStatement(String sql, boolean returnGeneratedKeys);

    boolean isReadOnly();

    TransactionIsolation getIsolation();
}
