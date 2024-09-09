package de.pdark.tutorial.cut;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.pdark.tutorial.cut.model.Order;
import de.pdark.tutorial.cut.model.User;

public class SlightlyBetterCode {
    
    private Connection connection;
    
    public SlightlyBetterCode() {
        this.connection = connectToDatabase();
    }

    private Connection connectToDatabase() {
        try {
            return DriverManager.getConnection("jdbc:....", "...", "..."); // Bad: Hardcoded URL and credentials 
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to connect to database", e); // Note: Always use exception chaining
        } 
    }

    public void doSomethingUseful() {
        var users = fetch();
        process(users);
    }

    private List<User> fetch() {
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
            throw new IllegalStateException("Unable to query database", e); 
        }
        return users;
    }

    private void process(List<User> users) {
        for(var user: users) {
            if (user.getName() != null) {
                if (user.getName().length() > 0) {
                    if (user.getName().trim().length() > 0) {
                        if (!user.getName().contains(" ")) {
                            var order = new Order();
                            order.setUser(user);
                            
                            save(order);
                        }
                    }
                }
            }
        }
    }

    private void save(Order order) {
        var sql2 = "insert into orders(user) values (?)";
        try (var stmt2 = connection.prepareStatement(sql2)) {
            stmt2.setString(1, order.getUser().getName()); // Note: In real code, this would be an ID
            if (!stmt2.execute()) {
                throw new SQLException("INSERT failed");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to store order in database", e);
        }
    }

    /*
     * This looks much better. We now have separated the three steps fetch, transform and store clearly.
     * 
     * This allows us to do the next refactoring: Moving those steps to helper classes.
     * 
     * Steps:
     * 
     * 1. Move the code in fetch() into a new class FetchUsers which gets the connection as constructor argument.
     * 2. Move the code in save() into a new class SaveOrder.
     * 
     * See MuchBetterCode
     */
}
