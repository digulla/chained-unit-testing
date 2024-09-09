package de.pdark.tutorial.cut;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import de.pdark.tutorial.cut.database.DatabaseConnectionExtension;
import de.pdark.tutorial.cut.database.PreparePreparedStatement;
import de.pdark.tutorial.cut.model.User;

class FetchUsersTest {
    
    /**
     * Use an extension to create a test database. To speed things up, I'm using an in-memory
     * database. On my machine, the three tests take less than a second to execute.
     * 
     * Another approach is to use a container or mix the two: Run as many tests as possible in the
     * in-memory database and only start a container for special features.
     * 
     * Or do both: Implement the extension in such a way that it can connect to several databases. 
     * Write a abstract test class which contains the tests and extend it once for each
     * database you need to support. That will run the same tests against each database.
     * 
     * If you then mark the slow container-based tests with a tag, you can run those only in the
     * CI pipeline. When you test locally, you only get the fast in-memory database but eventually,
     * everything will be tested against the production database as well.
     */
    @RegisterExtension
    DatabaseConnectionExtension database = new DatabaseConnectionExtension()
        .prepare("create table users (name varchar(256))");
    TestDataFactory testData = new TestDataFactory();

    @Test
    void emptyTable() {
        var connection = database.connect();
        assertUsers(Collections.emptyList(), connection);
    }
    
    /**
     * Start of the chain: Make sure we can fetch testData.users.valid() from the database.
     * 
     * The output (the user which we just read) will be used in the next step of the chain
     * to verify that we can process it. See GoodCodeTest.UserProcessingTest.validOrder()
     */
    @Test
    void singleUser() {
        // DRY: Use local variable to make sure we insert the expected data and then compare
        // against the correct expectations.
        User valid = testData.users.valid(); // Shared instance between tests
        var connection = database
                .prepare(insertUser(valid))
                .connect();

        assertUsers(
                Arrays.asList(valid),
                connection
        );
    }
    
    @Test
    void severalUsers() {
        User valid = testData.users.valid();
        User nameWithSpace = testData.users.nameWithSpace();
        
        var connection = database
                .prepare(insertUser(valid))
                .prepare(insertUser(nameWithSpace))
                .connect();
        assertUsers(
                Arrays.asList(valid, nameWithSpace),
                connection
        );
    }

    private PreparePreparedStatement insertUser(User user) {
        return new PreparePreparedStatement("insert into users (name) values (?)", user.getName());
    }

    private void assertUsers(List<User> expected, Connection connection) {
        var tool = new FetchUsers(connection);
        var users = tool.get();
        
        // Format both lists into a multi-line text so we can use the IDEs diff view to see any
        // discrepancies
        var actual = UserTestUtils.toString(users);
        assertEquals(UserTestUtils.toString(expected), actual);
        
        /* Some people would argue that we could simply use assertEquals(User, User) in a loop here.
         * There are several drawbacks to this approach:
         * 
         * - It doesn't work with ORM. In an ORM, equals() must only compare the business key,
         *   not the whole object.
         * - It doesn't work for every Java type. 
         * 
         *         new BigDecimal("1.0").equals(new BigDecimal(1))
         *         
         *   return false. The same is often true for double values because of rounding errors.
         * - When the test fails, you will now know which property made it fail.
         * - Comparing individual objects in a loop means you only see the first failure.
         *   It also makes it hard to see missing or additional objects or changes in the
         *   object order.
         * - We could write code which examines both lists and then prints explanations like
         *   "element #3 was expected in position #1" but that would have the following drawbacks:
         *     1. This would be very complicated code
         *     2. The developer needs to read and understand the messages. This often means to
         *        create a mental image of the lists which is error prone. My approach displays
         *        both lists in a diff view. The developer can use her/his full mental capacity
         *        to understand the issue, instead of wasting effort on building the lists in memory.
         *     3. Are you sure that the output of this code would always be correct? There are
         *        lots of edge cases which you have to consider: Duplicate elements, one or more
         *        missing elements, one or more additional elements ... on both sides. 
         * 
         * We can easily avoid all these problems by using a special toString() method for tests
         * and then collect the strings in a multi-line text. Every software developer can work
         * with diff views, that's our daily business. We can quickly see in those views when lines
         * shift, are duplicated or missing. Our brains are already trained for this. Any
         * other approach means that developers need to learn how to use this special
         * knowledge first and the knowledge won't be useful elsewhere. 
         */
    }
}
