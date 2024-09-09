package de.pdark.tutorial.cut.model;

public class User {

    private String name;
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "(name=" + name
                + ")";
    }
}
