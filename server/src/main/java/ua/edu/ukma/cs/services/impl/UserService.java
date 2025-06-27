package ua.edu.ukma.cs.services.impl;

import ua.edu.ukma.cs.database.transaction.TransactionDelegate;
import ua.edu.ukma.cs.exception.ForbiddenException;
import ua.edu.ukma.cs.api.request.RegisterUserRequestDto;
import ua.edu.ukma.cs.entity.UserEntity;
import ua.edu.ukma.cs.repository.UserRepository;
import ua.edu.ukma.cs.api.request.LoginUserRequestDto;
import ua.edu.ukma.cs.security.JwtServices;
import ua.edu.ukma.cs.security.PasswordHashGenerator;
import ua.edu.ukma.cs.validation.Validator;
import ua.edu.ukma.cs.exception.NotFoundException;

public class UserService {
    private final UserRepository repository;
    private final TransactionDelegate transactionDelegate;
    private final TransactionDelegate readOnlyTransactionDelegate;
    private final PasswordHashGenerator passwordHashGenerator;
    private final JwtServices jwtServices;

    public UserService(UserRepository repository, JwtServices jwtServices) {
        this(repository, jwtServices, new TransactionDelegate(), new TransactionDelegate(true));
    }

    public UserService(UserRepository repository, JwtServices jwtServices, TransactionDelegate transactionDelegate, TransactionDelegate readOnlyTransactionDelegate) {
        this.repository = repository;
        this.jwtServices = jwtServices;
        this.transactionDelegate = transactionDelegate;
        this.readOnlyTransactionDelegate = readOnlyTransactionDelegate;
        this.passwordHashGenerator = new PasswordHashGenerator();
    }

    public int register(RegisterUserRequestDto dto) {
        Validator.validate(dto)
                .notNullNotBlank(RegisterUserRequestDto::getUsername)
                .maxLength(RegisterUserRequestDto::getUsername, 64)
                .notNullNotBlank(RegisterUserRequestDto::getPassword)
                .maxLength(RegisterUserRequestDto::getPassword, 64);

        return transactionDelegate.runInTransaction(() -> {
            UserEntity entity = UserEntity.builder()
                    .username(dto.getUsername())
                    .passwordHash(passwordHashGenerator.hash(dto.getPassword()))
                    .build();

            validateEntity(entity);

            return repository.create(entity);
        });
    }

    public String login(LoginUserRequestDto dto) {
        Validator.validate(dto)
                .notNullNotBlank(LoginUserRequestDto::getUsername)
                .notNullNotBlank(LoginUserRequestDto::getPassword);

        return readOnlyTransactionDelegate.runInTransaction(() -> {
            UserEntity userEntity = repository.findByUsername(dto.getUsername())
                    .orElseThrow(NotFoundException::new);

            if (!passwordHashGenerator.check(dto.getPassword(), userEntity.getPasswordHash())) {
                throw new ForbiddenException();
            }

            return jwtServices.generateToken(userEntity);
        });
    }

    public UserEntity findById(int id) {
        return readOnlyTransactionDelegate.runInTransaction(() -> repository.findById(id).orElseThrow(NotFoundException::new));
    }

    private void validateEntity(UserEntity entity) {
        Validator.validate(entity)
                .unique(
                        UserEntity::getUsername,
                        () -> this.repository.findByUsername(entity.getUsername())
                );
    }
}
