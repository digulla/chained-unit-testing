package de.pdark.tutorial.cut.model;

public class Order {

    private User user;
    public void setUser(User user) {
        this.user = user;
    }
    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "(user=" + user
                + ")";
    }
}
