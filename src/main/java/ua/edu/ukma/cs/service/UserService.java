package ua.edu.ukma.cs.service;

import ua.edu.ukma.cs.database.transaction.TransactionDelegate;
import ua.edu.ukma.cs.exception.ForbiddenException;
import ua.edu.ukma.cs.request.RegisterUserRequestDto;
import ua.edu.ukma.cs.entity.UserEntity;
import ua.edu.ukma.cs.repository.UserRepository;
import ua.edu.ukma.cs.request.LoginUserRequestDto;
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

    public UserService(UserRepository repository) {
        this.repository = repository;
        this.transactionDelegate = new TransactionDelegate();
        this.readOnlyTransactionDelegate = new TransactionDelegate(true);
        this.passwordHashGenerator = new PasswordHashGenerator();
        this.jwtServices = new JwtServices();
    }

    public int register(RegisterUserRequestDto dto) {
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
        return readOnlyTransactionDelegate.runInTransaction(() -> {
            Validator.validate(dto)
                    .notNullNotBlank(LoginUserRequestDto::getUsername)
                    .notNullNotBlank(LoginUserRequestDto::getPassword);

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
                .notNullNotBlank(UserEntity::getUsername)
                .maxLength(UserEntity::getUsername, 64)
                .notNullNotBlank(UserEntity::getPasswordHash)
                .maxLength(UserEntity::getUsername, 250)
                .unique(
                        UserEntity::getUsername,
                        () -> this.repository.findByUsername(entity.getUsername())
                );
    }
}
