package ua.edu.ukma.cs.filter.base;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ConditionsBuilder {

    private final List<String> conditions = new ArrayList<>();

    public ConditionsBuilder exactEquals(Object value, String expression) {
        if(value == null) return this;
        conditions.add(String.format("%s = ?", expression));
        return this;
    }

    public ConditionsBuilder like(String value, String expression) {
        if (value == null || value.isBlank()) return this;
        conditions.add(String.format("LOWER(%s) LIKE ?", expression));
        return this;
    }

    public ConditionsBuilder min(Object value, String expression) {
        if (value == null) return this;
        conditions.add(String.format("%s >= ?", expression));
        return this;
    }

    public ConditionsBuilder max(Object value, String expression) {
        if (value == null) return this;
        conditions.add(String.format("%s <= ?", expression));
        return this;
    }
}
