package com.ravey.ai.user.start;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接测试
 *
 * @author ravey
 * @since 1.0.0
 */
public class DatabaseConnectionTest {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/user_center?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "123456";
        
        System.out.println("开始测试数据库连接...");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        
        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL驱动加载成功");
            
            // 建立连接
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("数据库连接成功!");
            System.out.println("连接信息: " + connection.getMetaData().getURL());
            
            // 关闭连接
            connection.close();
            System.out.println("连接已关闭");
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL驱动未找到: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            System.err.println("错误代码: " + e.getErrorCode());
            System.err.println("SQL状态: " + e.getSQLState());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("其他错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}