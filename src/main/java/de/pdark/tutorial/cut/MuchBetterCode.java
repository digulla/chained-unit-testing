package de.pdark.tutorial.cut;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import de.pdark.tutorial.cut.model.Order;
import de.pdark.tutorial.cut.model.User;

public class MuchBetterCode {
    
    private Connection connection;
    
    public MuchBetterCode() {
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
        var tool = new FetchUsers(connection);
        return tool.get();
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
        var tool = new SaveOrder(connection);
        tool.accept(order);
    }

    /*
     * When you look at this code, the next step should feel natural: Replace the fetch() and save() methods with
     * Supplier and Consumer which are passed as constructor arguments. That way, a unit test can easily replace
     * them.
     * 
     * See EasilyTestableCode for the result.
     */
}
