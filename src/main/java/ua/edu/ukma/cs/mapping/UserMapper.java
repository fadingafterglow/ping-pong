package ua.edu.ukma.cs.mapping;

import ua.edu.ukma.cs.dto.CreateUserRequestDto;
import ua.edu.ukma.cs.entity.UserEntity;

public class UserMapper {
    public static UserEntity map(CreateUserRequestDto dto) {
        var entity = new UserEntity();
        merge(entity, dto);
        return entity;
    }

    public static void merge(UserEntity to, CreateUserRequestDto from) {
        to.setUsername(from.getUsername());
        to.setPasswordHash(from.getPasswordHash());
    }
}
