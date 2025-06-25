package ua.edu.ukma.cs.database.context;

import lombok.Getter;
import ua.edu.ukma.cs.database.connection.DefaultConnectionFactory;
import ua.edu.ukma.cs.database.connection.IConnectionFactory;
import ua.edu.ukma.cs.database.transaction.DefaultTransactionManager;
import ua.edu.ukma.cs.database.transaction.TransactionManager;

import java.util.Properties;

public class PersistenceContext {

    @Getter
    private static PersistenceContext instance;

    @Getter
    private final TransactionManager transactionManager;

    private PersistenceContext(IConnectionFactory connectionFactory) {
        this.transactionManager = new DefaultTransactionManager(connectionFactory);
    }

    public synchronized static void init(Properties properties) {
        if (instance != null) return;
        IConnectionFactory connectionFactory = new DefaultConnectionFactory(properties);
        instance = new PersistenceContext(connectionFactory);
    }
}
