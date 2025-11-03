package com.mycompany.quanlythuvien.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 *
 * @author Tien
 */
public class DBConnector {
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Đọc file properties từ classpath
            Properties props = new Properties();
            props.load(DBConnector.class.getClassLoader().getResourceAsStream("application.properties"));

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.password");
            String driver = props.getProperty("db.driver");

            Class.forName(driver);

            if(user != null && !user.isEmpty()) { //dùng SQL Auth
                conn = DriverManager.getConnection(url, user, pass);
            } else { // dùng Windows Auth
                if(!url.contains("integratedSecurity=true")) {
                    url += ";integratedSecurity=true";
                }
                conn = DriverManager.getConnection(url);
            }

            System.out.println("Kết nối DB thành công!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }
}
