package ua.edu.ukma.cs.filter.base;

import lombok.SneakyThrows;
import ua.edu.ukma.cs.utils.TimeUtils;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.Locale;

public class ParametersSetter {

    private final PreparedStatement preparedStatement;
    private int index;

    public ParametersSetter(PreparedStatement preparedStatement, int index) {
        this.preparedStatement = preparedStatement;
        this.index = index;
    }

    @SneakyThrows
    public ParametersSetter setExactString(String value) {
        if (value == null || value.isBlank()) return this;
        preparedStatement.setString(index++, value);
        return this;
    }

    @SneakyThrows
    public ParametersSetter setLikeString(String value) {
        if (value == null || value.isBlank()) return this;
        preparedStatement.setString(index++, '%' + value.toLowerCase(Locale.ROOT) + '%');
        return this;
    }

    @SneakyThrows
    public ParametersSetter setInt(Integer value) {
        if (value == null) return this;
        preparedStatement.setInt(index++, value);
        return this;
    }

    @SneakyThrows
    public ParametersSetter setBigDecimal(BigDecimal value) {
        if (value == null) return this;
        preparedStatement.setBigDecimal(index++, value);
        return this;
    }

    @SneakyThrows
    public ParametersSetter setTimestamp(LocalDateTime value) {
        if(value == null) return this;
        preparedStatement.setTimestamp(index++, TimeUtils.mapToSqlTimestamp(value));
        return this;
    }
}
