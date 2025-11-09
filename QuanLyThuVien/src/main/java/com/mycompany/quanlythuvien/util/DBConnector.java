package com.mycompany.quanlythuvien.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 *
 * @author Tien
 */
public class DBConnector {

    private static HikariDataSource dataSource;

    static {
        try {
            Properties properties = new Properties();
            InputStream is = DBConnector.class.getClassLoader().getResourceAsStream("application.properties");
            properties.load(is);

            HikariConfig config = new HikariConfig();

            String user = properties.getProperty("db.user");
            String password = properties.getProperty("db.password");
            String url = properties.getProperty("db.url");

            config.setDriverClassName(properties.getProperty("db.driver"));
            config.setJdbcUrl(url);

            if(user != null && !user.isEmpty()) {
                // SQL server Auth
                config.setUsername(user);
                config.setPassword(password);
            } else {
                // Window Auth
                if (!url.contains("integratedSecurity=true")) {
                    config.setJdbcUrl(url + ";integratedSecurity=true;encrypt=true;trustServerCertificate=true;");
                }
            }

            // cấu hình pool
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(20000);

            dataSource = new HikariDataSource(config);

            System.out.println("Connection pool initialized successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws Exception {
       return dataSource.getConnection();
    }
}
