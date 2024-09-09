package de.pdark.tutorial.cut.database;

import java.util.Arrays;

public class PreparePreparedStatement {

    private String sql;
    private Object[] values;

    public PreparePreparedStatement(String sql, Object... values) {
        this.sql = sql;
        this.values = values;
    }
    
    public String getSql() {
        return sql;
    }
    
    public Object[] getValues() {
        return values;
    }
    
    @Override
    public String toString() {
        return sql + " " + Arrays.toString(values);
    }
}
