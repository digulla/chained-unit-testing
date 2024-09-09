package de.pdark.tutorial.cut;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import de.pdark.tutorial.cut.model.Order;
import de.pdark.tutorial.cut.model.User;

public class GoodCode {
    /** Can be simplified further now that we have lots of tests as time permits. */
    public static final Predicate<User> VALID_ORDER = user -> {
        if (user.getName() != null) {
            if (user.getName().length() > 0) {
                if (user.getName().trim().length() > 0) {
                    if (!user.getName().contains(" ")) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    };
    
    // No database connection anymore
    private Supplier<List<User>> fetch;
    private Consumer<Order> store;

    /**
     * Replace old constructor with this in existing code - minimal change, compiler will
     * let us know everything we need to do, zero risk.
     * 
     * Later, if we want to, we can clean this up further.
     */
    public static GoodCode create() {
        var connection = connectToDatabase();
        var fetch = new FetchUsers(connection);
        var store = new SaveOrder(connection);
        return new GoodCode(fetch, store);
    }

    private static Connection connectToDatabase() {
        try {
            return DriverManager.getConnection("jdbc:....", "...", "..."); // Bad: Hardcoded URL and credentials 
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to connect to database", e); // Note: Always use exception chaining
        } 
    }
    
    /** New constructor to make testing trivial. */
    public GoodCode(Supplier<List<User>> fetch, Consumer<Order> store) {
        this.fetch = fetch;
        this.store = store;
    }

    public void doSomethingUseful() {
        List<User> users = fetch.get();
        process(users);
    }

    private void process(List<User> users) { // TODO Could be merged into doSomethingUseful()
        for(var user: users) {
            if (VALID_ORDER.test(user)) {
                var order = new Order();
                order.setUser(user);
                
                store.accept(order);
            }
        }
    }

    /*
     * With GoodCodeTest, we can now try to find a better solution for connectToDatabase() and
     * other problems - if we want to. The main concern - testing the business logic - is already
     * covered, so we have achieved the main goal.
     * 
     * But we have one concern left: An integration test would tell us that fetching and storing
     * data in the database works. So far, we have just tested the middle of the whole chain of
     * operations.
     * 
     * The simple solution would be to create a database, fill in some test data for one
     * (or a few) valid case and run it. We don't need to write many integration tests because
     * the unit tests already cover most of them. Also, someone has tested the database, so we
     * don't need to do that again. Our concern is: Is the SQL correct, especially when I
     * have to change it later?
     * 
     * This is where the CUT approach comes in handy. Have a look at FetchUsersTest for my approach
     * of an integration test. Note how I use TestDataFactory to connect the integration test with
     * the unit tests in GoodCodeTest.ValidUserTest and SaveOrderTest.
     * 
     * Use your IDE to look for all places where TestDataFactory.UserDataFactory.valid()
     * is called to see how the integration tests and unit tests are linked. This object
     * is shared between tests to make sure that the "providing" code (fetch or transform)
     * can create this output and the "receiving" code (transform or store) can take
     * it as input. After all, "r = f(g(x));" is the same as "y = g(x); r = f(y);".
     * 
     * In a similar fashion, the SaveOrderTest use the orders from TestDataFactory.OrderDataFactory
     * which we have validated in GoodCodeTest.UserProcessingTest to make sure the result can be
     * stored in the database. Search for TestDataFactory.OrderDataFactory.valid() to see
     * this chain.
     * 
     * This way, we can prove that all three steps work correctly without having a single test
     * which runs all three steps. We can now cut chains of operations by storing the inputs and
     * outputs in shared variables and verify the operation by writing a test that makes sure that
     * the code under test can produce the same content as the shared variable and another test
     * that proves the shared variable can be used as input for the next step in the chain.
     * 
     * A similar approach can be used for all kinds of external sources and targets: REST services,
     * message queues, etc. In every case, we record the data which we receive or send plus
     * we add a test that actually tries to receive or send that exact data to the real
     * service. By using tags or System properties + Assume.assumeTrue(), we can control
     * when these tests run.
     * 
     * For example, imagine you have a brittle external system where the test data changes all
     * the time. Create a test to export the test data which only runs on demand. Now you can
     * run it manually whenever you need to update the test data. The test will often fail but
     * it will only fail on demand. That means you have now gated your system against
     * unexpected changes. That means you can work efficiently most of the time and plan when
     * to break your build. In contrast, without this, you would either not be able to test
     * this part OR it would randomly break your build when the external system is not available
     * or when someone suddenly makes changes that affect you.
     * 
     * Another bonus point: If the tests fail, it doesn't affect you immediately. You can now
     * look at the problems and find solutions. Without this, those tests might prevent you from
     * getting a green build with the usual side effects: Unable to test emergency production
     * patches, work has to stop until they are fixed or they get ignored, the tests are disabled
     * "until we can fix them" and technical debt piles up.
     * 
     * With this approach, you can look at the failures. If they are trivial, you can fix them
     * right away (like updating the test data plus fixing the tests that break because of it).
     * Or you can create a ticket with your findings and just continue to work with the test data
     * that you already have in your source code.
     * 
     * More advantages of this approach:
     * 
     * - Usually, you work on a single feature. That means while you write code, you usually only
     *   have to run the tests in a single class or package. This makes the code-test-loop
     *   very fast.
     * 
     * There are drawbacks to this approach:
     * 
     * - If the input data changes, you sometimes have to run all the tests several times
     *   until the changes have propagated through the chain. This is why it is so important
     *   that it's trivial to update the expected test output by either using shared objects
     *   (so you fix several tests with one change) or by using strings which you can
     *   simply copy&paste. 
     */
}
