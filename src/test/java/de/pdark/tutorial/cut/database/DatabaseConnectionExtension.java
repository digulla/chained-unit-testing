package de.pdark.tutorial.cut.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnectionExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionExtension.class);
    
    private Connection connection;
    private String testClass;
    private String dbName;
    private String user = "sa";
    private String password = "";
    private Map<String, String> options = new LinkedHashMap<>();
    private List<PreparePreparedStatement> prepare = new ArrayList<>();

    static {
        try {
            DriverManager.registerDriver(new org.h2.Driver());
        } catch (SQLException e) {
            throw new DatabaseException("Unable to register H2 driver", e);
        }
    }
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        testClass = context.getTestClass().map(it -> it.getSimpleName() + ".").orElse("");
        dbName = context.getRequiredTestMethod().getName();
    }
    
    public DatabaseConnectionExtension option(String name, String value) {
        options.put(name, value);
        return this;
    }
    
    public Connection connect() {
        if (connection == null) { // Not multi-threaded, so this is safe
            connection = configure(doConnect());
            
            prepareDatabase();
            commit();
        }
        
        return connection;
    }

    public void commit() {
        assertNotNull(connection, "please call connect(), first");
        
        try {
            log.debug("{}{}: COMMIT", testClass, dbName);
            connection.commit();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to commit transaction for " + connection, e);
        }
    }

    private Connection configure(Connection conn) {
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to configure connection " + conn, e);
        }
        
        return conn;
    }

    private void prepareDatabase() {
        prepare.forEach(it -> {
            log.debug("{}{}: Preparing database: {}", testClass, dbName, it);
            var sql = it.getSql();
            try (var stmt = connection.prepareStatement(sql)) {
                var values = it.getValues();
                for (int i = 0; i < values.length; i++) {
                    stmt.setObject(i + 1, values[i]);
                }

                if (!stmt.execute()) {
                    throw new SQLException("execute() return false");
                }
            } catch (SQLException e) {
                new DatabaseException("Error excecuting SQL: " + sql, e);
            }
        });
        
    }

    Connection doConnect() {
        var url = "jdbc:h2:mem:" + dbName + collectOptions();
        log.debug("{}{}: Connecting to {}", testClass, dbName, url);

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseException("Unable to connect to database\nurl=" + url + "\nuser=" + user);
        }
    }
    
    public DatabaseConnectionExtension prepare(String sql, Object... values) {
        prepare.add(new PreparePreparedStatement(sql, values));
        return this;
    }
    
    public DatabaseConnectionExtension prepare(PreparePreparedStatement statement) {
        prepare.add(statement);
        return this;
    }
    
    private String collectOptions() {
        if (options.isEmpty()) {
            return "";
        }
        
        return options.entrySet()
                .stream()
                .map(it -> String.format("%s=%s", it.getKey(), it.getValue()))
                .collect(Collectors.joining(";", ";", ""));
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public void assertTableContent(String expected, String... tables) {
        var actual = new StringBuilder();
        
        Arrays.stream(tables).forEach(it -> dumpTable(actual, it));
        
        assertEquals(expected.stripTrailing(), actual.toString());
    }

    private void dumpTable(StringBuilder result, String tableName) {
        var sql = String.format("select * from %s", tableName);
        dumpQuery(result, sql);
    }

    public String dumpQuery(String sql) {
        var result = new StringBuilder();
        dumpQuery(result, sql);
        return result.toString();
    }
    
    private void dumpQuery(StringBuilder result, String sql) {
        result.append(sql).append(":\n");

        log.debug("{}{}: dumpQuery sql={}", testClass, dbName, sql);
        try (var stmt = connect().prepareStatement(sql)) {
            try (var ps = stmt.executeQuery()) {
                dumpResultSet(result, ps);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to execute " + sql, e);
        }
    }

    public void dumpResultSet(StringBuilder result, ResultSet ps) throws SQLException {
        dumpHeader(result, ps);
        
        int count = 0;
        while (ps.next()) {
            result.append("\n");
            dumpRow(result, ps);
            count ++;
        }
        
        if (count == 0) {
            result.append("\n*no data*");
        }
    }

    private void dumpHeader(StringBuilder result, ResultSet ps) throws SQLException {
        var metaData = ps.getMetaData();
        var delim = "";
        for (int i=1; i<=metaData.getColumnCount(); i ++) {
            var name = metaData.getColumnName(i);
            
            result.append(delim).append(name);
            delim = ",";
        }
    }
    
    private void dumpRow(StringBuilder result, ResultSet ps) throws SQLException {
        var metaData = ps.getMetaData();
        var delim = "";
        for (int i=1; i<=metaData.getColumnCount(); i ++) {
            var value = ps.getObject(i);
            
            result.append(delim).append(value);
            delim = ",";
        }
    }
}
