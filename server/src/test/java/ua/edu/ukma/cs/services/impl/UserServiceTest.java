package ua.edu.ukma.cs.services.impl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ua.edu.ukma.cs.api.request.LoginUserRequestDto;
import ua.edu.ukma.cs.api.request.RegisterUserRequestDto;
import ua.edu.ukma.cs.database.context.PersistenceContext;
import ua.edu.ukma.cs.database.transaction.TransactionDelegate;
import ua.edu.ukma.cs.entity.UserEntity;
import ua.edu.ukma.cs.exception.ForbiddenException;
import ua.edu.ukma.cs.exception.NotFoundException;
import ua.edu.ukma.cs.exception.ValidationException;
import ua.edu.ukma.cs.repository.UserRepository;
import ua.edu.ukma.cs.security.JwtServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private final JwtServices jwtServices = new JwtServices(getProperties());
    private TransactionDelegate transactionDelegate;

    @SneakyThrows
    private static Properties getProperties() {
        var props = new Properties();
        props.load(PersistenceContext.class.getResourceAsStream("/application.properties"));
        return props;
    }

    @BeforeEach
    public void beforeEach() {
        transactionDelegate = mock(TransactionDelegate.class);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionDelegate).runInTransaction(any(Runnable.class));

        when(transactionDelegate.runInTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(0);
                    return supplier.get();
                });
    }

    @ParameterizedTest
    @CsvSource({
            ", qwerty",
            "qwerty, ",
            "'', qwerty",
            "qwerty, ''",
            "dawwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww, qwerty",
            "qwerty, dawwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww",
    })
    public void register_givenInvalidDto_shouldThrow(String username, String password) {
        RegisterUserRequestDto dto = RegisterUserRequestDto.builder()
                .username(username)
                .password(password)
                .build();

        UserService service = getUserService(mock(UserRepository.class));

        assertThrows(ValidationException.class, () -> service.register(dto));
    }

    @Test
    public void register_givenNotUniqueUsername_shouldThrow() {
        String username = "qwerty";
        RegisterUserRequestDto dto = RegisterUserRequestDto.builder()
                .username(username)
                .password(username)
                .build();

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(new UserEntity(1, username, username)));

        UserService service = getUserService(userRepository);

        assertThrows(ValidationException.class, () -> service.register(dto));
    }

    @Test
    public void register_givenValidDtoAndUniqueUsername_shouldReturnId() {
        String username = "qwerty";
        RegisterUserRequestDto dto = RegisterUserRequestDto.builder()
                .username(username)
                .password(username)
                .build();

        UserService service = getUserService(getUserRepositoryMockOnList(new ArrayList<>()));

        int id = assertDoesNotThrow(() -> service.register(dto));
        assertEquals(1, id);
    }

    @ParameterizedTest
    @CsvSource({
            ", qwerty",
            "qwerty, ",
            "'', qwerty",
            "qwerty, ''",
    })
    public void login_givenInvalidDto_shouldThrow(String username, String password) {
        LoginUserRequestDto dto = LoginUserRequestDto.builder()
                .username(username)
                .password(password)
                .build();

        UserService service = getUserService(mock(UserRepository.class));

        assertThrows(ValidationException.class, () -> service.login(dto));
    }

    @Test
    public void login_givenNotExistingUser_shouldThrow() {
        String username = "qwerty";
        LoginUserRequestDto dto = LoginUserRequestDto.builder()
                .username(username)
                .password(username)
                .build();

        UserService service = getUserService(getUserRepositoryMockOnList(new ArrayList<>()));

        assertThrows(NotFoundException.class, () -> service.login(dto));
    }

    @Test
    public void login_givenInvalidPassword_shouldThrow() {
        String username = "qwerty";
        RegisterUserRequestDto registerDto = RegisterUserRequestDto.builder()
                .username(username)
                .password(username)
                .build();

        UserService service = getUserService(getUserRepositoryMockOnList(new ArrayList<>()));

        assertDoesNotThrow(() -> service.register(registerDto));

        LoginUserRequestDto loginDto = LoginUserRequestDto.builder()
                .username(username)
                .password(username + "1")
                .build();

        assertThrows(ForbiddenException.class, () -> service.login(loginDto));
    }

    @Test
    public void login_givenExistingUserAndValidPassword_shouldReturnValidToken() {
        String username = "qwerty";
        RegisterUserRequestDto registerDto = RegisterUserRequestDto.builder()
                .username(username)
                .password(username)
                .build();

        UserService service = getUserService(getUserRepositoryMockOnList(new ArrayList<>()));

        assertDoesNotThrow(() -> service.register(registerDto));

        LoginUserRequestDto loginDto = LoginUserRequestDto.builder()
                .username(username)
                .password(username)
                .build();

        String token = assertDoesNotThrow(() -> service.login(loginDto));
        assertDoesNotThrow(() -> jwtServices.verifyToken(token));
    }

    private UserRepository getUserRepositoryMockOnList(List<UserEntity> users) {
        UserRepository userRepository = mock(UserRepository.class);

        when(userRepository.create(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity entity = invocation.getArgument(0);
                    entity.setId(users.size() + 1);
                    users.add(entity);
                    return entity.getId();
                });

        when(userRepository.findById(anyInt()))
                .thenAnswer(invocation -> {
                    int id = invocation.getArgument(0);
                    return users.stream()
                            .filter(user -> user.getId() == id)
                            .findFirst();
                });

        when(userRepository.findByUsername(anyString()))
                .thenAnswer(invocation -> {
                    String username = invocation.getArgument(0);
                    return users.stream()
                            .filter(user -> username.equals(user.getUsername()))
                            .findFirst();
                });

        return userRepository;
    }

    private UserService getUserService(UserRepository userRepository) {
        return new UserService(userRepository, jwtServices, transactionDelegate, transactionDelegate);
    }
}