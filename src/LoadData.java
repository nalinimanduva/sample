import java.io.*;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import org.apache.commons.lang.StringUtils;

public class LoadData {

    static final String DRIVER = "oracle.jdbc.driver.OracleDriver";

    static final String USERNAME = "imageuser";

    static final String PASSWORD = "p3n13l";

    //static final String STRING = "jdbc:oracle:thin:@10.1.0.1:1522:pspatch";
    static final String STRING = "jdbc:oracle:thin:@10.1.0.5:1521:tatrng";

    static final String batchNum = "BATCH13";

    static final String cmisCompanyId = "5";

    private static Connection conn = null;

    public static void main(String[] args) {
        //BufferedReader input =  new BufferedReader(new FileReader("/Users/charlesweems/Downloads/TAPImages/index.dat"));
        StringTokenizer st = null;
        try {
            BufferedReader input = new BufferedReader(new FileReader("/Users/charlesweems/" + batchNum + "/INDEX.DAT"));
            //BufferedReader input =  new BufferedReader(new FileReader("/Users/charlesweems/Workspaces/Workbook3.csv"));
            //not declared within while loop
            String line = input.readLine();
            conn = getDatabaseConnection();
            int lineNum = 0;
            System.out.println(line);
            while ((line = input.readLine()) != null) {
                Vector currentLine = new Vector();
                System.out.println("***  Line #" + lineNum);
                st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {
                    String nextElement = st.nextToken("|");
                    if (!StringUtils.isBlank(nextElement)) {
                        currentLine.addElement(nextElement);
                    } else {
                        currentLine.addElement("--");
                    }
                }
                System.out.println(currentLine.capacity());
                //System.out.println("***  Filename = " + currentLine.get(0) + "   Date = " + currentLine.get(2) + "  ***");
                // 	            String dateChange = (String) currentLine.get(2);
                // 	            dateChange = dateChange.replace("-", "/");
                //get the company id
                //get the document_id by using the sequence
                //int documentId = createID("document_seq");
                int documentId = createID("document_data_seq");
                //insert data into the document_data table
                insertIntoDocumentData((String) currentLine.get(1), documentId);
                //insert data into the search_data table
                insertIntoSearchData(documentId, (String) currentLine.get(3), (String) currentLine.get(4), (String) currentLine.get(5));
                // 	        	
                // 	        	PreparedStatement StatementRecordset = conn.prepareStatement(sql);
                // 	    		StatementRecordset.execute(sql);
                // 	    		
                // 	    		StatementRecordset.close();
                lineNum++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                StringTokenizer st = null;
                try {
                    //make sure that the connection is closed
                    disconnectdb();
                    System.out.println("***** Connection Closed OK *****");
                } catch (Exception e) {
                    System.out.println("***** Connection Closed Error: Check Main Method *****");
                    e.printStackTrace();
                }
            }
        }
    }

    public static void insertIntoDocumentData(String filename, int documentId) {
        String sql = "insert into document_data values (" + "5001047, " + documentId + ", 'CMIS/" + batchNum + "/" + filename + "', " + cmisCompanyId + ", 0, 0, sysdate, 0, '')";
        System.out.println(sql);
        StringTokenizer st = null;
        try {
            executeUpdate(sql);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void insertIntoSearchData(int documentId, String settlementDate, String scheduleNumber, String amount) {
        String sql1 = "insert into search_data values (228, " + documentId + ", " + cmisCompanyId + ", '" + amount + "')";
        String sql2 = "insert into search_data values (221, " + documentId + ", " + cmisCompanyId + ", '" + scheduleNumber + "')";
        String sql3 = "insert into search_data values (220, " + documentId + ", " + cmisCompanyId + ", '" + settlementDate + "')";
        //System.out.println(sql);
        System.out.println(sql1);
        System.out.println(sql2);
        System.out.println(sql3);
        StringTokenizer st = null;
        try {
            executeUpdate(sql1);
            executeUpdate(sql2);
            executeUpdate(sql3);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Connection getDatabaseConnection() {
        if (conn == null) {
            StringTokenizer st = null;
            try {
                Driver Driverapprove = (Driver) Class.forName(DRIVER).newInstance();
                conn = DriverManager.getConnection(STRING, USERNAME, PASSWORD);
            //logger.info("New Database Connection Successful");
            } catch (Exception e) {
            //logger.error("Error in getting context : "+e.getMessage()); 
            }
        } else {
        //logger.info("Connection Already Exists . . . returning old connection  ");
        }
        return conn;
    }

    public static PreparedStatement connectdb(String sql) {
        PreparedStatement preparedStatement = null;
        StringTokenizer st = null;
        try {
            //logger.info("Requesting new connection(object)..");  
            //Connection conn = getDatabaseConnection();  
            preparedStatement = conn.prepareStatement(sql);
            if (preparedStatement == null) {
            //logger.info("Cannot create Statement..");
            }
        } catch (Exception e) {
        //logger.error("connectdb() : Exception: " + e.getMessage());
        }
        return preparedStatement;
    }

    public static void executeUpdate(String updateStatement) throws Exception {
        PreparedStatement preparedStatement = null;
        long start = 0L;
        long end = 0L;
        StringTokenizer st = null;
        try {
            start = System.currentTimeMillis();
            //logger.info(updateStatement);
            preparedStatement = connectdb(updateStatement);
            preparedStatement.executeUpdate();
            end = System.currentTimeMillis();
        //logger.debug("Update Execute took "+(end-start)+" ms.");
        } catch (Exception e) {
            //logger.error("executeUpdate() : Exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            StringTokenizer st = null;
            try {
                preparedStatement.close();
                end = System.currentTimeMillis();
            //logger.debug("Open and close of Update connection took "+(end-start)+" ms.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static int createID(String sequence) {
        int indexId = 0;
        StringTokenizer st = null;
        try {
            indexId = getNewIdFromSequence(sequence);
        } catch (Exception e) {
            //logger.error("createID() : Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return indexId;
    }

    public static int getNewIdFromSequence(String sequence) throws Exception {
        int id = 0;
        Vector hashMapList = new Vector();
        StringTokenizer st = null;
        try {
            //preparedStatement = connectdb();
            //resultSet = preparedStatement.executeQuery("select  " + sequence + ".nextval from dual");
            //            //ResultSet resultSet = getResultSet("select  " + sequence + ".nextval from dual");
            hashMapList = getResultSet("select  " + sequence + ".nextval from dual");
            //	for(int x = 0; x < hashMapList.size(); x++)
            //{
            HashMap hashMap = new HashMap();
            hashMap = (HashMap) hashMapList.get(0);
            id = Integer.parseInt((String) hashMap.get("NEXTVAL"));
        //}
        //			while (resultSet.next())
        //	        {
        //				id = Integer.parseInt(resultSet.getString("nextval"));
        //	       
        //	        }
        } catch (Exception e) {
            e.printStackTrace();
        //logger.error("getNewIdFromSequence() : Exception: " + e.getMessage());
        }
        return id;
    }

    public static Vector getResultSet(String sqlStatement) throws Exception {
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = connectdb(sqlStatement);
        Vector hashMapList = new Vector();
        //Connection conn = getDatabaseConnection();
        long start = 0L;
        long end = 0L;
        StringTokenizer st = null;
        try {
            start = System.currentTimeMillis();
            //preparedStatement = conn.createStatement();
            resultSet = preparedStatement.executeQuery();
            end = System.currentTimeMillis();
            //logger.debug("Query Execute took "+(end-start)+" ms.");
            //get the number of columns and get their names
            Vector columnNames = new Vector();
            for (int y = 0; y < resultSet.getMetaData().getColumnCount(); y++) {
                columnNames.addElement(resultSet.getMetaData().getColumnName(y + 1));
            }
            while (resultSet.next()) {
                HashMap hashMap = new HashMap();
                //now put the values in a hashmap column name/value
                for (int z = 0; z < columnNames.size(); z++) {
                    hashMap.put(columnNames.get(z), resultSet.getString(z + 1));
                //logger.debug("hashMap -->"+hashMap);
                }
                hashMapList.addElement(hashMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //logger.error("getResultSet() : Exception: " + e.getMessage());
            throw e;
        } finally {
            StringTokenizer st = null;
            try {
                preparedStatement.close();
                resultSet.close();
                //make sure that the connection is closed
                //disconnectdb();
                end = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
            //logger.error("Exception in finally : " + e.getMessage());
            }
        }
        return hashMapList;
    }

    public static void disconnectdb() {
        StringTokenizer st = null;
        try {
            // logger.info("Closing connection...");
            conn.close();
            //logger.info("Done");
            conn = null;
        } catch (Exception e) {
            e.printStackTrace();
        //logger.error("disconnectdb() : Exception: " + e.getMessage());
        } finally {
            _vf_safeClose(st);
        }
    }

    public static void _vf_safeClose(StringTokenizer arg0) {
        if (arg0 != null) {
            try {
                arg0.close();
            } catch (IOException e) {
            // TODO VioFixer: handle your logging here
            }
        }
    }
}
