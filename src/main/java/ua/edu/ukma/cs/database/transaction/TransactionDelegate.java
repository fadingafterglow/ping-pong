package ua.edu.ukma.cs.database.transaction;

import ua.edu.ukma.cs.database.context.PersistenceContext;

import java.util.function.Supplier;

public class TransactionDelegate {

    private final boolean isReadOnly;
    private final TransactionIsolation isolation;
    private final TransactionManager transactionManager;

    public TransactionDelegate() {
        this(false, TransactionIsolation.READ_COMMITTED);
    }

    public TransactionDelegate(boolean isReadOnly) {
        this(isReadOnly, TransactionIsolation.READ_COMMITTED);
    }

    public TransactionDelegate(boolean isReadOnly, TransactionIsolation isolation) {
        this.isReadOnly = isReadOnly;
        this.isolation = isolation;
        this.transactionManager = PersistenceContext.getInstance().getTransactionManager();
    }

    public void runInTransaction(Runnable runnable) {
        if (transactionManager.isTransactionActive()) {
            runnable.run();
            return;
        }
        try {
            transactionManager.beginTransaction(isReadOnly, isolation);
            runnable.run();
            transactionManager.commitCurrent();
        } catch (Exception e) {
            transactionManager.rollbackCurrent();
            throw e;
        }
    }

    public <T> T runInTransaction(Supplier<T> supplier) {
        if (transactionManager.isTransactionActive()) {
            return supplier.get();
        }
        try {
            transactionManager.beginTransaction(isReadOnly, isolation);
            T result = supplier.get();
            transactionManager.commitCurrent();
            return result;
        } catch (Exception e) {
            transactionManager.rollbackCurrent();
            throw e;
        }
    }
}
