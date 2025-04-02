package org.example.exchangeproject;


import java.sql.*;

public class Database {
    private static Database instance;
    private String url = "jdbc:postgresql://localhost:5432/curriency_db";
    private String uname = "postgres";
    private String password = "1234";
    private Connection connection;
    private Statement statement;

    private Database() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");

        connection = DriverManager.getConnection(url,uname,password);

        statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
    }

    public static Database getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null){
            instance = new Database();
        }

        return instance;
    }

    public void executeUpdateSt(String sql) throws SQLException {
        this.statement.executeUpdate(sql);
    }

    public ResultSet executeQuerySt(String sql) throws SQLException {
        return this.statement.executeQuery(sql);
    }


    public void close() throws SQLException {
        this.connection.close();
    }

}
