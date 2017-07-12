package com.peniel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;
import java.sql.*;
import org.apache.log4j.Logger;

public class DatabaseConnection {

    public DatabaseConnection() {
    }

    private static Connection conn = null;

    private static Logger logger = Logger.getLogger(DatabaseConnection.class);

    public static Connection getDatabaseConnection() {
        String str = "";
        String usn = "";
        String pwd = "";
        if (conn == null) {
            try {
                javax.naming.Context ctx = new  javax.naming.InitialContext();
                str = (String) ctx.lookup("java:comp/env/connection");
                usn = (String) ctx.lookup("java:comp/env/dbusername");
                pwd = (String) ctx.lookup("java:comp/env/dbpassword");
                Class.forName("oracle.jdbc.driver.OracleDriver");
                conn = DriverManager.getConnection(str, usn, pwd);
            } catch (Exception e) {
                logger.error("Error in getting context : " + e.getMessage());
            }
        } else {
            logger.info("Connection Already Exists . . . returning old connection  ");
        }
        return conn;
    }

    public static Connection getDBConnection() {
        String str = "";
        String usn = "";
        String pwd = "";
        //	{
        try {
            javax.naming.Context ctx = new  javax.naming.InitialContext();
            str = (String) ctx.lookup("java:comp/env/connection");
            usn = (String) ctx.lookup("java:comp/env/dbusername");
            pwd = (String) ctx.lookup("java:comp/env/dbpassword");
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(str, usn, pwd);
        } catch (Exception e) {
            logger.error("Error in getting context : " + e.getMessage());
        }
        /*}
		else
		{
			logger.info("Connection Already Exists . . . returning old connection  ");
		}*/
        return conn;
    }

    public static void setCommit(boolean vl) {
        try {
            conn.setAutoCommit(vl);
        } catch (Exception e) {
            logger.error("connectdb().setCommit : Exception: " + e.getMessage());
        }
    }

    public static void doCommit() {
        try {
            conn.commit();
        } catch (Exception e) {
            logger.error("connectdb().doCommit : Exception: " + e.getMessage());
        }
    }

    public static PreparedStatement connectdb(String sql) {
        PreparedStatement preparedStatement = null;
        //Connection conn = null;
        try {
            conn = getDatabaseConnection();
            preparedStatement = conn.prepareStatement(sql);
            if (preparedStatement == null)
                logger.info("Cannot create Statement..");
        } catch (Exception e) {
            System.out.println("connectdb() : Exception: " + e.getMessage());
        }
        return preparedStatement;
    }

    public static PreparedStatement executePreparedStmt(String sql, Connection con) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = con.prepareStatement(sql);
            if (preparedStatement == null)
                logger.info("Cannot create Statement..");
        } catch (Exception e) {
            System.out.println("executePreparedStmt() : Exception: " + e.getCause());
        }
        return preparedStatement;
    }

    public static Vector getResultSet(String sqlStatement) throws Exception {
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = connectdb(sqlStatement);
        Vector hashMapList = new  Vector();
        long start = 0L;
        long end = 0L;
        try {
            start = System.currentTimeMillis();
            resultSet = preparedStatement.executeQuery();
            end = System.currentTimeMillis();
            //get the number of columns and get their names
            Vector columnNames = new  Vector();
            for (int y = 0; y < resultSet.getMetaData().getColumnCount(); y++) {
                columnNames.addElement(resultSet.getMetaData().getColumnName(y + 1));
            }
            while (resultSet.next()) {
                HashMap hashMap = new  HashMap();
                //now put the values in a hashmap column name/value
                for (int z = 0; z < columnNames.size(); z++) {
                    System.out.println("columnNames.get(z)-->" + columnNames.get(z) + "<-- resultSet.getString(z+1) -->" + resultSet.getString(z + 1) + "<---");
                    hashMap.put(columnNames.get(z), resultSet.getString(z + 1));
                }
                hashMapList.addElement(hashMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getResultSet() : Exception: " + e.getMessage());
            throw e;
        } finally {
            try {
                preparedStatement.close();
                resultSet.close();
                end = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in finally : " + e.getMessage());
            }
        }
        return hashMapList;
    }

    public static int getNewIdFromSequence(String sequence) throws Exception {
        int id = 0;
        Vector hashMapList = new  Vector();
        try {
            hashMapList = getResultSet("select  " + sequence + ".nextval from dual");
            //{
            HashMap hashMap = new  HashMap();
            hashMap = (HashMap) hashMapList.get(0);
            id = Integer.parseInt((String) hashMap.get("NEXTVAL"));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getNewIdFromSequence() : Exception: " + e.getMessage());
        }
        return id;
    }

    public void executeUpdate(String updateStatement) throws Exception {
        PreparedStatement preparedStatement = null;
        long start = 0L;
        long end = 0L;
        try {
            //logger.info(updateStatement);
            preparedStatement = connectdb(updateStatement);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println("executeUpdate() : Exception: " + e.getCause());
            e.printStackTrace();
            throw e;
        } finally {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void executeUpdateQuery(String updateStatement, Connection con) throws Exception {
        PreparedStatement preparedStatement = null;
        try {
            //preparedStatement = connectdb(updateStatement);
            preparedStatement = con.prepareStatement(updateStatement);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println("executeUpdateQuery() : Exception: " + e.getMessage());
            throw e;
        }
    }

    public int execute_Update(String updateStatement) throws Exception {
        PreparedStatement preparedStatement = null;
        long start = 0L;
        long end = 0L;
        try {
            start = System.currentTimeMillis();
            //logger.info(updateStatement);
            preparedStatement = connectdb(updateStatement);
            int rowCount = preparedStatement.executeUpdate();
            end = System.currentTimeMillis();
            // logger.debug("Update Execute took "+(end-start)+" ms.");
            return rowCount;
        } catch (Exception e) {
            logger.error("executeUpdate() : Exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            try {
                preparedStatement.close();
                //make sure that the connection is closed
                disconnectdb();
                end = System.currentTimeMillis();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static int createID(String sequence) {
        int indexId = 0;
        try {
            indexId = getNewIdFromSequence(sequence);
        } catch (Exception e) {
            logger.error("createID() : Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return indexId;
    }

    public static int getSingleValue(String sqlStmt) throws Exception {
        int id = 0;
        Vector hashMapList = new  Vector();
        try {
            hashMapList = getResultSet(sqlStmt);
            HashMap hashMap = new  HashMap();
            hashMap = (HashMap) hashMapList.get(0);
            id = Integer.parseInt((String) hashMap.get("RESULT"));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getNewIdFromSequence() : Exception: " + e.getMessage());
            throw e;
        }
        return id;
    }

    public static void disconnectdb() {
        try {
            // logger.info("Closing connection...");
            if (conn != null) {
                conn.close();
                System.out.println("Connection closed");
                conn = null;
            }
        } catch (Exception e) {
            System.out.println("disconnectdb() : Exception: " + e.getMessage());
        }
    }
}
