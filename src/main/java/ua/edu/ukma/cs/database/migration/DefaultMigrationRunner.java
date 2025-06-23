package ua.edu.ukma.cs.database.migration;

import ua.edu.ukma.cs.database.context.PersistenceContext;
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

public class DefaultMigrationRunner implements MigrationRunner {

    private static final String DEFAULT_MIGRATIONS_INDEX = "/migrations/index";
    private final String migrationsIndex;

    public DefaultMigrationRunner() {
        this(DEFAULT_MIGRATIONS_INDEX);
    }

    public DefaultMigrationRunner(String migrationsIndex) {
        this.migrationsIndex = migrationsIndex;
    }

    @Override
    public void runMigrations() {
        TransactionDelegate transactionDelegate = new TransactionDelegate();
        for (String migration: findMigrations()) {
            transactionDelegate.runInTransaction(() -> runMigration(migration));
        }
    }

    private void runMigration(String migration) {
        TransactionManager transactionManager = PersistenceContext.getInstance().getTransactionManager();
        try (Statement statement = transactionManager.currentTransaction().createStatement()) {
            statement.execute(migration);
        }
        catch (SQLException e) {
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
        }
        catch (IOException e) {
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
