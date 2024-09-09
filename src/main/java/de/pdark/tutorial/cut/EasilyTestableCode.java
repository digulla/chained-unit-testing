package de.pdark.tutorial.cut;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.pdark.tutorial.cut.model.Order;
import de.pdark.tutorial.cut.model.User;

public class EasilyTestableCode {
    
    // No database connection anymore
    private Supplier<List<User>> fetch;
    private Consumer<Order> store;

    /** Replace constructor with this in existing code. */
    public static EasilyTestableCode create() {
        var connection = connectToDatabase();
        var fetch = new FetchUsers(connection);
        var store = new SaveOrder(connection);
        return new EasilyTestableCode(fetch, store);
    }

    private static Connection connectToDatabase() {
        try {
            return DriverManager.getConnection("jdbc:....", "...", "..."); // Bad: Hardcoded URL and credentials 
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to connect to database", e); // Note: Always use exception chaining
        } 
    }
    
    public EasilyTestableCode(Supplier<List<User>> fetch, Consumer<Order> store) {
        this.fetch = fetch;
        this.store = store;
    }

    public void doSomethingUseful() {
        List<User> users = fetch.get();
        process(users);
    }

    private void process(List<User> users) {
        for(var user: users) {
            if (user.getName() != null) {
                if (user.getName().length() > 0) {
                    if (user.getName().trim().length() > 0) {
                        if (!user.getName().contains(" ")) {
                            var order = new Order();
                            order.setUser(user);
                            
                            store.accept(order);
                        }
                    }
                }
            }
        }
    }

    /*
     * So far, we just did simple refactorings which were low risk. It's now time to write some unit tests.
     * 
     * See EasilyTestableCodeTest.
     * 
     * We have spent maybe one hour to refactor the code and write tests. Those tests take less then a second to execute.
     * Imagine how much more time you would have spent writing integration tests for the original code and how slow they
     * would have been.
     * 
     * This concludes part 1 of the CUT tutorial: How to refactor code to make it easier to test.
     * 
     * Since we have this, we can now do more risky refactorings like getting rid of the deeply nested if()s
     * in the process() loop.
     * 
     * In order to do this, combine all the if()s in a single Predicate<User>. This then allows us to test the new
     * predicate directly without the need for the suppliers and consumers.
     * 
     * See GoodCode.
     */
}
