package ua.edu.ukma.cs.repository;

import ua.edu.ukma.cs.database.migration.MigrationRunner;
import ua.edu.ukma.cs.database.transaction.TransactionDelegate;
import ua.edu.ukma.cs.database.transaction.TransactionManager;
import ua.edu.ukma.cs.exception.DataBaseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TestMigrationRunner implements MigrationRunner {

    private static final String DEFAULT_MIGRATIONS_INDEX = "/migrations/index";
    private final String migrationsIndex;

    private final TransactionDelegate transactionDelegate;
    private final TransactionManager transactionManager;

    public TestMigrationRunner(TransactionManager transactionManager, TransactionDelegate transactionDelegate) {
        this(transactionManager, transactionDelegate, DEFAULT_MIGRATIONS_INDEX);
    }

    public TestMigrationRunner(TransactionManager transactionManager, TransactionDelegate transactionDelegate, String migrationsIndex) {
        this.migrationsIndex = migrationsIndex;
        this.transactionDelegate = transactionDelegate;
        this.transactionManager = transactionManager;
    }

    @Override
    public void runMigrations() {
        for (String migration : findMigrations()) {
            transactionDelegate.runInTransaction(() -> runMigration(migration));
        }
    }

    private void runMigration(String migration) {
        try (Statement statement = transactionManager.currentTransaction().createStatement()) {
            statement.execute(migration);
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    private List<String> findMigrations() {
        try (InputStream index = getClass().getResourceAsStream(migrationsIndex)) {
            List<String> migrationFiles = readAllLines(index);
            List<String> result = new ArrayList<>(migrationFiles.size());
            for (String migrationFile : migrationFiles) {
                try (InputStream migration = getClass().getResourceAsStream(migrationFile)) {
                    if (migration == null) continue;
                    result.add(readAll(migration));
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readAll(InputStream inputStream) throws IOException {
        byte[] file = inputStream.readAllBytes();
        return new String(file, StandardCharsets.UTF_8);
    }

    private List<String> readAllLines(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream)).lines().toList();
    }
}
