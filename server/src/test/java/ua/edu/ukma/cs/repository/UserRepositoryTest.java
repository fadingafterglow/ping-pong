package ua.edu.ukma.cs.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.edu.ukma.cs.database.transaction.DefaultTransactionManager;
import ua.edu.ukma.cs.database.transaction.TransactionDelegate;
import ua.edu.ukma.cs.database.transaction.TransactionIsolation;
import ua.edu.ukma.cs.entity.UserEntity;
import ua.edu.ukma.cs.exception.DataBaseException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserRepositoryTest {
    @Container
    private final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("ping_pong")
            .withUsername("postgres")
            .withPassword("root");

    private UserRepository repository;

    @BeforeEach
    public void beforeEach() {
        var connectionFactory = new TestConnectionFactory(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        var transactionManager = new DefaultTransactionManager(connectionFactory);
        var transactionDelegate = new TransactionDelegate(false, TransactionIsolation.READ_UNCOMMITTED, transactionManager);

        new TestMigrationRunner(transactionManager, transactionDelegate).runMigrations();

        repository = new UserRepository(transactionManager);

        transactionManager.beginTransaction(false, TransactionIsolation.READ_UNCOMMITTED);
    }

    @ParameterizedTest
    @CsvSource({
            ",",
            ", qwerty",
            "qwerty, "
    })
    public void create_givenInvalidEntity_shouldThrow(String username, String passwordHash) {
        UserEntity entity = UserEntity.builder()
                .username(username)
                .passwordHash(passwordHash)
                .build();

        assertThrows(DataBaseException.class, () -> repository.create(entity));
    }

    @Test
    public void create_givenValidEntity_shouldReturnGeneratedId() {
        UserEntity entity = UserEntity.builder()
                .username("qwerty")
                .passwordHash("qwerty")
                .build();

        int id = assertDoesNotThrow(() -> repository.create(entity));

        assertEquals(1, id);
    }

    @Test
    public void findById_givenNotExistingId_shouldReturnEmptyOptional() {
        Optional<UserEntity> retrievedEntity = assertDoesNotThrow(() -> repository.findById(-1));

        assertTrue(retrievedEntity.isEmpty());
    }

    @Test
    public void findById_givenExistingId_shouldReturnEntity() {
        UserEntity entity = UserEntity.builder()
                .username("qwerty")
                .passwordHash("qwerty")
                .build();
        int id = assertDoesNotThrow(() -> repository.create(entity));

        Optional<UserEntity> retrievedEntity = assertDoesNotThrow(() -> repository.findById(id));

        assertTrue(retrievedEntity.isPresent());
        assertEquals(entity.getUsername(), retrievedEntity.get().getUsername());
        assertEquals(entity.getPasswordHash(), retrievedEntity.get().getPasswordHash());
    }

    @Test
    public void findByUsername_givenNotExistingUsername_shouldReturnEmptyOptional() {
        Optional<UserEntity> retrievedEntity = assertDoesNotThrow(() -> repository.findByUsername("none"));

        assertTrue(retrievedEntity.isEmpty());
    }

    @Test
    public void findByUsername_givenExistingUsername_shouldReturnEntity() {
        String username = "qwerty";
        UserEntity entity = UserEntity.builder()
                .username(username)
                .passwordHash("qwerty")
                .build();
        assertDoesNotThrow(() -> repository.create(entity));

        Optional<UserEntity> retrievedEntity = assertDoesNotThrow(() -> repository.findByUsername(username));

        assertTrue(retrievedEntity.isPresent());
        assertEquals(entity.getUsername(), retrievedEntity.get().getUsername());
        assertEquals(entity.getPasswordHash(), retrievedEntity.get().getPasswordHash());
    }
}