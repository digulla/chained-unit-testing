package de.pdark.tutorial.cut.database;

public class DatabaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Exception cause) {
        super(message, cause);
    }
}
