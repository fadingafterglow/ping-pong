package ua.edu.ukma.cs.filter;

import lombok.experimental.SuperBuilder;
import ua.edu.ukma.cs.filter.base.BaseFilter;
import ua.edu.ukma.cs.filter.base.ConditionsBuilder;
import ua.edu.ukma.cs.filter.base.ParametersSetter;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

@SuperBuilder
public class UserExactEqualityFilter extends BaseFilter {
    private Integer id;
    private String username;

    @Override
    protected List<String> formWhereConditions(Map<String, String> fieldExpressionMap) {
        return new ConditionsBuilder()
                .equals(id, fieldExpressionMap.get("id"))
                .equals(username, fieldExpressionMap.get("username"))
                .getConditions();
    }

    @Override
    public void setParameters(PreparedStatement st, int parametersIndexOffset) {
        new ParametersSetter(st, parametersIndexOffset)
                .setInt(id)
                .setExactString(username);
    }
}
