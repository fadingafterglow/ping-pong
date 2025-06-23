package ua.edu.ukma.cs.filter;

import lombok.experimental.SuperBuilder;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuperBuilder
public abstract class BaseFilter {

    protected int page;
    protected int size;
    protected String sortBy;
    protected boolean descendingOrder;

    public String addFiltering(String originalQuery, Map<String, String> fieldExpressionMap) {
        StringBuilder query = new StringBuilder(originalQuery);
        formWhereClause(query, fieldExpressionMap);
        return query.toString();
    }

    public String addFilteringAndPagination(String originalQuery, Map<String, String> fieldExpressionMap) {
        StringBuilder query = new StringBuilder(originalQuery);
        formWhereClause(query, fieldExpressionMap);
        formPaginationClause(query, fieldExpressionMap);
        return query.toString();
    }

    private void formWhereClause(StringBuilder query, Map<String, String> fieldExpressionMap) {
        List<String> conditions = formWhereConditions(fieldExpressionMap);
        if (conditions.isEmpty()) return;
        String clause = conditions.stream()
                .collect(Collectors.joining(" AND ", "\nWHERE ", ""));
        query.append(clause);
    }

    protected abstract List<String> formWhereConditions(Map<String, String> fieldExpressionMap);

    private void formPaginationClause(StringBuilder query, Map<String, String> fieldExpressionMap) {
        String orderExpression = fieldExpressionMap.get(Objects.requireNonNullElse(sortBy, "id"));
        if (orderExpression != null)
            query.append("\nORDER BY ").append(orderExpression).append(" ").append(descendingOrder ? "DESC" : "ASC");
        if (size > 0) {
            query.append("\nLIMIT ").append(size);
            if (page > 0)
                query.append("\nOFFSET ").append(page * size);
        }
    }

    public void setParameters(PreparedStatement st) {
        setParameters(st, 1);
    }

    public abstract void setParameters(PreparedStatement st, int parametersIndexOffset);
}
