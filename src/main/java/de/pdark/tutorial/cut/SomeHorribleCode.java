package de.pdark.tutorial.cut;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import de.pdark.tutorial.cut.model.Order;
import de.pdark.tutorial.cut.model.User;

public class SomeHorribleCode {
    
    private Connection connection;
    
    public SomeHorribleCode() {
        this.connection = connectToDatabase();
    }

    private Connection connectToDatabase() {
        try {
            return DriverManager.getConnection("jdbc:....", "...", "..."); // Bad: Hardcoded URL and credentials 
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to connect to database"); // Bad: Without exception chaining, there is no clue why this failed.
        } 
    }

    public void doSomethingUseful() {
        // First fetch some data from the database.
        var sql = "select * from users";
        try (var stmt = connection.prepareStatement(sql)) {
            try (var ps = stmt.executeQuery()) {
                var users = new ArrayList<User>();
                while (ps.next()) {
                    var user = new User();
                    user.setName(ps.getString("name"));
                    // copy all the other fields...
                    
                    users.add(user);
                }
                
                // Now do something with the data
                for(var user: users) {
                    if (user.getName() != null) {
                        if (user.getName().length() > 0) {
                            if (user.getName().trim().length() > 0) {
                                if (!user.getName().contains(" ")) {
                                    var order = new Order();
                                    order.setUser(user);
                                    
                                    sql = "insert into orders(user) values (?)"; // Bad: Reuse of variable above
                                    try (var stmt2 = connection.prepareStatement(sql)) { // Note: Java doesn't allow us to reuse "stmt" here. Imagine how that could go wrong otherwise.
                                        stmt2.setString(1, order.getUser().getName()); // Note: In real code, this would be an ID
                                        if (!stmt2.execute()) {
                                            throw new SQLException("Unable to save order");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to query database"); // This is either useless or wrong (when the exception is thrown by the INSERT statement)
        }
    }

    /*
     * Some thoughts about the code above:
     * 
     * - It mixes a lot of concerns:
     *     - Reading from an external source
     *     - Validation
     *     - processing data
     *     - writing to an external target
     * - The error handling is broken
     * 
     * As a result, the code is hard to read and even harder to test.
     * 
     * Imagine you want to test the validation: You would have to
     * 
     * - start a database
     * - somehow hack connectToDatabase() or the database to be able to get a connection
     * - set up the two tables
     * - insert several users into the database, possibly resetting the database for each test
     * 
     * and the only clue whether the validation worked is an entry in the "orders" table. 
     * This would be slow and hard to maintain.
     * 
     * Ideally, we would have tests to refactor this code but we don't. So what is the next step?
     * 
     * Let us rely on the built-in refactoring support of your IDE and be more careful when making changes.
     * We might break something on the way but the new code will be much easier to test. That way, we will be
     * faster overall since we can spend the time we saved writing the integration tests first on new, fast
     * unit tests.
     * 
     * 1. Create a new local variable "sql2" for the insert statement. Make sure you use it in the prepareStatement().
     * 2. Select all code necessary to save the order and refactor it into a new method "save()".
     * 3. Select the whole for loop and refactor it into a new method "process()".
     * 4. Get rid of the now useless comment before the call to "process()"
     * 5. Now a tricky change: Move the call to process() to the end of doSomethingUseful(), that is outside of the try catch.
     * 6. Fix the first compile error by moving the declaration of "users" to the top.
     * 7. Fix the unhandled exception by adding catch to the try in save().
     * 8. Delete the "throws SQLException" on save().
     * 9. Refactor the code to fetch the users into a new method "fetch()".
     * 10. Get rid of the useless comment in doSomethingUseful()
     * 11. Find all throw statements and make sure the cause is passed to the new exception.
     * 12. Replace "ArrayList" used as return or parameter types by "List".
     * 
     * Have a look at SlightlyBetterCode to see my version.
     */
}
