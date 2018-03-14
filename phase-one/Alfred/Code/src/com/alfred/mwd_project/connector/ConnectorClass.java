package com.alfred.mwd_project.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectorClass {
	
	public static Connection connectToDB(){
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(ConnectionParams.DB_NAME, ConnectionParams.USER, ConnectionParams.PASSWD);
		} catch (ClassNotFoundException e) {
			System.out.println("Driver not found.");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("Connection url incorrect. Please check the parameters.");
			e.printStackTrace();
		} 
		return conn;
	}
	
	public static void closeConnection(Connection conn){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
