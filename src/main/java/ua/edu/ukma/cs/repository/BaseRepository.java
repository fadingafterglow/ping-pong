package ua.edu.ukma.cs.repository;

import ua.edu.ukma.cs.database.context.PersistenceContext;
import ua.edu.ukma.cs.database.transaction.TransactionManager;

public abstract class BaseRepository {

    protected final TransactionManager transactionManager;

    public BaseRepository() {
        this.transactionManager = PersistenceContext.getInstance().getTransactionManager();
    }
}
