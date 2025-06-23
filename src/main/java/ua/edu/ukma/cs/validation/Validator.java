package ua.edu.ukma.cs.validation;

import ua.edu.ukma.cs.exception.ValidationException;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Validator<T> {
    private final T validatable;

    public static <T> Validator<T> validate(T dto) {
        return new Validator<>(dto);
    }

    public Validator(T validatable) {
        this.validatable = validatable;
    }

    public <TField> Validator<T> notNull(Function<T, TField> fieldAccessor) {
        return notNull(fieldAccessor, "Field is null");
    }

    public <TField> Validator<T> notNull(Function<T, TField> fieldAccessor, String msg) {
        if (fieldAccessor.apply(validatable) == null) {
            throw new ValidationException(msg);
        }
        return this;
    }

    public Validator<T> notBlank(Function<T, String> fieldAccessor) {
        return notBlank(fieldAccessor, "Field is blank");
    }

    public Validator<T> notBlank(Function<T, String> fieldAccessor, String msg) {
        if (fieldAccessor.apply(validatable).isBlank()) {
            throw new ValidationException(msg);
        }
        return this;
    }

    public Validator<T> maxLength(Function<T, String> fieldAccessor, int maxLength) {
        return maxLength(fieldAccessor, maxLength, "Field length is more than " + maxLength);
    }

    public Validator<T> maxLength(Function<T, String> fieldAccessor, int maxLength, String msg) {
        if (fieldAccessor.apply(validatable).length() > maxLength) {
            throw new ValidationException(msg);
        }
        return this;
    }

    public <TField> Validator<T> unique(Function<T, TField> fieldAccessor, Supplier<Optional<T>> findDuplicateRequest) {
        return unique(fieldAccessor, findDuplicateRequest, "Field is not unique");
    }

    public <TField> Validator<T> unique(Function<T, TField> fieldAccessor, Supplier<Optional<T>> findDuplicateRequest, String msg) {
        boolean isNotUnique = findDuplicateRequest.get()
                .map(e -> fieldAccessor.apply(e).equals(fieldAccessor.apply(validatable)))
                .orElse(false);

        if (isNotUnique) {
            throw new ValidationException(msg);
        }

        return this;
    }
}
