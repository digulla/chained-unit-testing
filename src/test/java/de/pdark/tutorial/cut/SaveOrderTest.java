package de.pdark.tutorial.cut;

import java.sql.Connection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import de.pdark.tutorial.cut.database.DatabaseConnectionExtension;

class SaveOrderTest {
    @RegisterExtension
    DatabaseConnectionExtension database = new DatabaseConnectionExtension()
        .prepare("create table orders (user_name varchar(256))");
    TestDataFactory testData = new TestDataFactory();

    /**
     * Last step in the chain of tests. This stores the object in the database which 
     * GoodCodeTest.UserProcessingTest.validOrder() produced as output.
     */
    @Test
    void validOrder() {
        var valid = testData.orders.valid();
        
        Connection connection = database.connect();

        var tool = new SaveOrder(connection);
        tool.accept(valid);
        
        database.assertTableContent(
                """
                select * from orders:
                USER_NAME
                valid
                """,
                "orders");
    }

}
