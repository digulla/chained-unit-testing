package de.pdark.tutorial.cut;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

import de.pdark.tutorial.cut.model.Order;

public class SaveOrder implements Consumer<Order> {

    private Connection connection;

    public SaveOrder(Connection connection) {
        this.connection = connection;
    }

    public void accept(Order order) {
        var sql = "insert into orders(user_name) values (?)";
        try (var stmt2 = connection.prepareStatement(sql)) {
            stmt2.setString(1, order.getUser().getName()); // Note: In real code, this would be an ID
            if (stmt2.executeUpdate() != 1) {
                throw new SQLException("INSERT failed");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to store order in database\nsql: " + sql + "\n order: " + order, e); // Again put as much information into an error message as possible
        }
    }
}
