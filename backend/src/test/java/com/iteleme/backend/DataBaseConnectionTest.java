package com.iteleme.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testConnection() throws SQLException {
        Connection connection = dataSource.getConnection();

        assertNotNull(connection);
        assertFalse(connection.isClosed());

        System.out.println("数据库连接成功！");
        System.out.println("数据库：" + connection.getCatalog());

        connection.close();
    }
}