package de.pdark.tutorial.cut;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.pdark.tutorial.cut.model.Order;
import de.pdark.tutorial.cut.model.User;

public class OrderTestUtils {

    public static String toString(Order order) {
        return "user=" + Optional.of(order).map(Order::getUser).map(User::getName).orElse("null"); 
    }
    
    public static String toString(List<Order> orders) {
        return orders.stream()
            .map(OrderTestUtils::toString)
            .collect(Collectors.joining("\n"));
    }
}
