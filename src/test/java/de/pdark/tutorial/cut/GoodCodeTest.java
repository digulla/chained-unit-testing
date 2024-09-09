package de.pdark.tutorial.cut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.pdark.tutorial.cut.model.Order;
import de.pdark.tutorial.cut.model.User;

class GoodCodeTest {
    User user = new User();
    TestDataFactory testData = new TestDataFactory();

    /** Directly test validation. Those don't need the suppliers and consumers. */
    @Nested
    class ValidUserTest {
        @Test
        void noName() {
            user.setName(null); // Not necessary but to document intent
            assertInvalidUserName();
        }

        @Test
        void emptyName() {
            user.setName("");
            assertInvalidUserName();
        }
        
        @Test
        void blankName() {
            user.setName(" ");
            assertInvalidUserName();
        }
        
        @Test
        void nameWithWhitespace() {
            user.setName("\r\n\t");
            assertInvalidUserName();
        }
        
        @Test
        void nameWithSpace() {
            user = testData.users.nameWithSpace();
            assertInvalidUserName();
        }
        
        @Test
        void success() {
            user = testData.users.valid();
            assertValidUserName();
        }
        
        // Use custom assertions to make tests more readable and avoid code duplication.
        private void assertInvalidUserName() {
            assertFalse(
                    GoodCode.VALID_ORDER.test(user),
                    () -> "User with this name should fail: [" + user.getName() + "]"
            ); 
        }
        
        private void assertValidUserName() {
            assertTrue(
                    GoodCode.VALID_ORDER.test(user),
                    () -> "User with this name should pass: [" + user.getName() + "]"
            ); 
        }
    }
    
    /** Tests that verify the looping */
    @Nested
    class UserProcessingTest {
        
        private List<Order> orders = new ArrayList<>();

        @Test
        void testNull() {
            user = null;
            var tool = create(users(user));
            assertThrows(
                NullPointerException.class,
                () -> tool.doSomethingUseful()
            );
        }
        
        @Test
        void noUsers() {
            var tool = create(users());
            tool.doSomethingUseful();
            assertOrders();
        }

        /**
         * Middle of the chain: After verifying that we can read this user from the database in
         * FetchUsersTest.singleUser(), we make sure we can transform it into an Order.
         * 
         * The 
         */
        @Test
        void validOrder() {
            user = testData.users.valid();
            var tool = create(users(user));
            tool.doSomethingUseful();
            assertOrders(testData.orders.valid());
        }
        
        @Test
        void severalOrders() {
            user = testData.users.valid();
            var user2 = testData.users.valid2();
            var tool = create(users(user, user2));
            tool.doSomethingUseful();
            assertOrders(testData.orders.valid(), testData.orders.valid2());
        }
        
        /**
         * Assert that the result is correct.
         * 
         * The approach with comparing multi-line strings has several advantages:
         * 
         * - It's easy to understand and maintain.
         * - If the assert fails, it contains a lot of information to help you understand the issue.
         * - I'm using input data above to create the expected result. In more complex cases,
         *   you will use a string literal. If the code produces new output, you can quickly fix the
         *   test by copying the new correct output and pasting it into the string literal.
         */
        private void assertOrders(Order... expected) {
            var expectedString = OrderTestUtils.toString(Arrays.asList(expected));
            var actual = OrderTestUtils.toString(orders);
            
            // All IDEs will display a diff when you compare two multi-line strings.
            // This makes it easy to display the results in a useful way.
            assertEquals(expectedString, actual);
        }

        // Helper methods to write compact tests
        private EasilyTestableCode create(Supplier<List<User>> fetch) {
            Consumer<Order> store = it -> {
                orders.add(it);
            };
            return new EasilyTestableCode(fetch, store);
        }

        private Supplier<List<User>> users(User... users) {
            return () -> Arrays.asList(users);
        }
    }
}
