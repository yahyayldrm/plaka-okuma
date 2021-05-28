package com.plakatanima;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public DBConnection() {

    }
	
	
	 private final String url = "jdbc:postgresql://localhost/plakaTanima";
	 private final String user = "postgres";
	 private final String password = "123yahya";
	 
	 
	    public Connection getDBConnection() {
	        Connection con = null;
	        try {
	            Class.forName("org.postgresql.Driver").newInstance();
	            con = DriverManager.getConnection(url, user, password);
	            System.out.println("PostgreSQL server baðlantýsý baþarýlý.");
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
	            System.out.println(ex.getMessage());
	        }

	        return con;
	    }
	 
	  
}
