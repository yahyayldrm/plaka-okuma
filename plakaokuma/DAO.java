package com.plakatanima;

import java.sql.Connection;
import com.plakatanima.DBConnection;

public class DAO {

    private DBConnection db;
    private Connection conn;

    public DBConnection getDb() {
        if (db == null) {
            db = new DBConnection();
        }
        return db;
    }

    public void setDb(DBConnection db) {
        this.db = db;
    }

    public Connection getConn() {
        if (conn == null) {
            conn = getDb().getDBConnection();
        }

        return conn;
    }
}
