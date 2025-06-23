package ua.edu.ukma.cs.service;

import ua.edu.ukma.cs.database.transaction.TransactionDelegate;
import ua.edu.ukma.cs.dto.CreateUserRequestDto;
import ua.edu.ukma.cs.entity.UserEntity;
import ua.edu.ukma.cs.mapping.UserMapper;
import ua.edu.ukma.cs.repository.UserRepository;
import ua.edu.ukma.cs.validation.Validator;

import java.util.NoSuchElementException;

public class UserService {
    private final UserRepository repository;
    private final TransactionDelegate transactionDelegate;
    private final TransactionDelegate readOnlyTransactionDelegate;

    public UserService(UserRepository repository) {
        this.repository = repository;
        this.transactionDelegate = new TransactionDelegate();
        this.readOnlyTransactionDelegate = new TransactionDelegate(true);
    }

    public int create(CreateUserRequestDto dto) {
        return transactionDelegate.runInTransaction(() -> {
            UserEntity entity = UserMapper.map(dto);
            validateEntity(entity);
            return repository.createUser(entity);
        });
    }

    public UserEntity findById(int id) {
        return readOnlyTransactionDelegate.runInTransaction(() -> repository.findById(id).orElseThrow(NoSuchElementException::new));
    }

    private void validateEntity(UserEntity entity) {
        Validator.validate(entity)
                .notNull(UserEntity::getUsername)
                .notBlank(UserEntity::getUsername)
                .maxLength(UserEntity::getUsername, 64)
                .notNull(UserEntity::getPasswordHash)
                .notBlank(UserEntity::getPasswordHash)
                .maxLength(UserEntity::getUsername, 250)
                .unique(
                        UserEntity::getUsername,
                        () -> this.repository.findByUsername(entity.getUsername())
                );
    }
}
