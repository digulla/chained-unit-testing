package de.pdark.tutorial.cut;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.pdark.tutorial.cut.model.Order;
import de.pdark.tutorial.cut.model.User;

class EasilyTestableCodeTest {

    private User user = new User();
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
        assertOrders("");
    }
    
    @Test
    void noName() {
        user.setName(null); // Not necessary but to document intent
        var tool = create(users(user));
        tool.doSomethingUseful();
        assertOrders("");
    }
    
    @Test
    void emptyName() {
        user.setName("");
        var tool = create(users(user));
        tool.doSomethingUseful();
        assertOrders("");
    }
    
    @Test
    void blankName() {
        user.setName(" ");
        var tool = create(users(user));
        tool.doSomethingUseful();
        assertOrders("");
    }
    
    @Test
    void nameWithWhitespace() {
        user.setName("\r\n\t");
        var tool = create(users(user));
        tool.doSomethingUseful();
        assertOrders("");
    }
    
    @Test
    void nameWithBlank() {
        user.setName("a b");
        var tool = create(users(user));
        tool.doSomethingUseful();
        assertOrders("");
    }
    
    @Test
    void success() {
        user.setName("valid");
        var tool = create(users(user));
        tool.doSomethingUseful();
        assertOrders(user.getName());
    }
    
    @Test
    void severalOrders() {
        user.setName("valid");
        var user2 = new User();
        user2.setName("valid2");
        var tool = create(users(user, user2));
        tool.doSomethingUseful();
        assertOrders(user.getName() + "\n" + user2.getName());
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
    private void assertOrders(String expected) {
        var actual = orders.stream()
                .map(it ->
                    Optional.of(it.getUser())
                        .map(User::getName)
                        .orElse("*user or user.name is null*")
                )
                .collect(Collectors.joining("\n")); // Note: Don't use the platform line ending here, it will break the test when someone else runs it.
        
        // All IDEs will display a diff when you compare two multi-line strings.
        // This makes it easy to display the results in a useful way.
        assertEquals(expected, actual);
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
