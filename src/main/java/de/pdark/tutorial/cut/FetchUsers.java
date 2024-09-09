package de.pdark.tutorial.cut;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.pdark.tutorial.cut.model.User;

public class FetchUsers implements Supplier<List<User>> {

    private Connection connection;

    public FetchUsers(Connection connection) {
        this.connection = connection;
    }

    public List<User> get() {
        var users = new ArrayList<User>();
        var sql = "select * from users";
        try (var stmt = connection.prepareStatement(sql)) {
            try (var ps = stmt.executeQuery()) {
                while (ps.next()) {
                    var user = new User();
                    user.setName(ps.getString("name"));
                    // copy all the other fields...
                    
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to query database: " + sql, e); // Try to include as much information in the error message as possible. Here: The SQL which failed. 
        }
        return users;
    }
}
