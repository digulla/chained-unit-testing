package de.pdark.tutorial.cut;

import de.pdark.tutorial.cut.model.Order;
import de.pdark.tutorial.cut.model.User;

public class TestDataFactory {

    public UserDataFactory users = new UserDataFactory();
    public OrderDataFactory orders = new OrderDataFactory();
    
    class UserDataFactory {
        public User valid() {
            var result = new User();
            result.setName("valid");
            return result;
        }
        
        public User valid2() {
            var result = new User();
            result.setName("valid2");
            return result;
        }
        
        public User nameWithSpace() {
            var result = new User();
            result.setName("a b");
            return result;
        }
    }
    
    class OrderDataFactory {
        public Order valid() {
            var result = new Order();
            result.setUser(users.valid());
            return result;
        }
        
        public Order valid2() {
            var result = new Order();
            result.setUser(users.valid2());
            return result;
        }
    }
}
