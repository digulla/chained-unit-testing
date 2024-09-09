package de.pdark.tutorial.cut;

import java.util.List;
import java.util.stream.Collectors;

import de.pdark.tutorial.cut.model.User;

public class UserTestUtils {

    public static String toString(User user) {
        // We can also use User.toString() but this way, the tests are decoupled from the default toString() implementation.
        // That means changes in the default toString() implementation don't break the tests but it also means
        // you might have several places to update when more fields are added IF they are important for the tests.
        // Again, this gives you options to pick depending on your situation.
        return "name=" + user.getName(); 
    }
    
    public static String toString(List<User> users) {
        return users.stream()
            .map(UserTestUtils::toString)
            .collect(Collectors.joining("\n"));
    }
}
