import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import com.peniel.batch.MyPropWithinClasspath;
import com.peniel.bus.vo.PropertyBatch;
import com.peniel.common.DBManager;
import com.peniel.common.DbManagerValues;
import com.peniel.common.PenielException;
import com.peniel.common.dbobject;
import com.peniel.dao.FolderDAO;
import com.peniel.dao.PropertyDAO;
import com.peniel.database.DatabaseConnection;




public class BatchPropertyFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		BatchPropertyFile batchStatusUpdate = new BatchPropertyFile();
		batchStatusUpdate.statusUpdate();
	
	}
	
	/**
	 * Retrieve Status from Arcis for the asset number
	 * and update the status in the database.
	 */
	public void statusUpdate(){
		
		Connection conn;
        String DRIVER;
        String USERNAME;
        String PASSWORD;
        String STRING;
        conn = null;
        
        
        PreparedStatement pstmt = null;
        PreparedStatement pstmt1 = null;
        DRIVER = "oracle.jdbc.driver.OracleDriver";
        USERNAME = "img";
        PASSWORD ="img";
        STRING = "jdbc:oracle:thin:@172.31.2.111:1521:PSPATCH2";
        long seq = 0 ;
        long comid = 1;
        int id ;
		boolean verification = false;
		PropertyDAO propertyDao = new PropertyDAO();
       
        try{
	            Class.forName(DRIVER).newInstance();
	            conn = DriverManager.getConnection(STRING, USERNAME, PASSWORD);
	            
	          
        	Scanner sc = new Scanner(new File("Section 232 Portfolio as of 1.4.2017_v1.csv"));
        	PropertyBatch property=null;
        	Set<PropertyBatch> empList = new HashSet<PropertyBatch>();
        	if(sc.hasNext()==true)
        	{
        		sc.nextLine();
        	}
        	while (sc.hasNext()) {
        		property=new PropertyBatch();
        		String[] str = sc.nextLine().split(",");
        		   property.setProperty_id(str[0]);
                  property.setProperty_name_text(str[1].replaceAll("`", " ").replaceAll("'", "").trim());
                  empList.add(property);
        		} 
        			
        			System.out.println("size"+empList.size());
        	sc.close();
        	
        	
        	
        	
        	for(PropertyBatch prop:empList){
           
    			seq = getNextId();
    			
    		
    		
            if( seq != 0)
            {
            
					String sqlStatement = "insert into property (id,company_Id, property_Id,fha_Num,account_Exec,workload_Mgr,lender_Num,"+
	        		               	      " lender_Name,servicer_Num,servicer_Name,property_Name,city,state,zip,congressional_Area,trouble_Code,reac_Scrore,"+
	        			                  " active_Deac_Assignement) values (" +seq+","+ comid + ", '" + prop.getProperty_id() + "'" +
	        			                  ", '" + null + "', '" + null +"', '" + null +"', '" + null +"', '" + null +
	        			                  "', '" + null +"','"+null +"', '" + prop.getProperty_name_text() +"', '" + null +"', '" + null +"', '" + null +"', '"  + 
	        			                  null +"', '" + null +"', '" + null+"','"+null+"' )";
					System.out.println("insert into property query : " + sqlStatement);
					try {
					 pstmt = conn.prepareStatement(sqlStatement);
				     pstmt.executeUpdate();
					} finally {
					    try { pstmt.close(); } catch (Exception ignore) { }
					}
				     verification=true;	       
				
				
            }
    		
            if(verification){
            	
            	String folderName = "/"+prop.getProperty_id()+"/";
            	 id = getMax()+1;
            	
            	System.out.println("id"+id);
            	try {
            	 pstmt1 = conn.prepareStatement("insert into folders (Company_id, folder_id, folder_name) values " +
      	                "(" + comid + ", " + id + ", '" + folderName + "')");
                  pstmt1.executeUpdate();
            	}
                  finally {
					    try { pstmt1.close(); } catch (Exception ignore) { }
					}
            }
    		
        }
        	conn.commit();		
		} catch (Exception e)
		{       try {
			conn.rollback();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			System.out.println("Exception in call");
			e.printStackTrace();
		}
        
		finally{
			try{
				if(pstmt != null){
	    			pstmt.close();
	    			pstmt = null;
		        }
				if(pstmt1 != null){
	    			pstmt1.close();
	    			pstmt1 = null;
		        }
	    		    		
	    		conn.close();
				conn = null;
				
				System.out.println("********* Done ********");
			}catch(Exception exc){
				System.out.println("Batch Status Update : Error during closing connection -->"+exc.getMessage());
			}	
        }	
		
	}
	
	 public static Connection getDatabaseConnection() {
	    	MyPropWithinClasspath mpc=new MyPropWithinClasspath();
	        Connection conn = null;

	        if (conn == null) {
	            try {
	                Class.forName("oracle.jdbc.driver.OracleDriver");
	                conn = DriverManager.getConnection(mpc.getPropertyValue("connection"), mpc.getPropertyValue("dbusername"), mpc.getPropertyValue("dbpassword"));

	                // logger.info("New Database Connection Successful");
	            } catch (Exception e) {
	                System.out.println("Error in getting context : " + e.getMessage());

	            }
	        }
	        return conn;
	    }

	
	public  int  getMax() throws SQLException
	{	
		
		   Connection conn = null;
	        PreparedStatement pstmt =null;
	        ResultSet rs =null;
		int folderId = 0;
		try {		
			conn = getDatabaseConnection();
			String sqlStatement =" select max(folder_id) as RESULT from Folders";
			System.out.println("sqlStatement: "+sqlStatement);			
		       pstmt = conn.prepareStatement(sqlStatement);
		        rs = pstmt.executeQuery();

		        while (rs.next()) {
		        	folderId = rs.getInt("RESULT");
		        }
			
			//folderId = new DatabaseConnection().getSingleValue(sqlStatement);
			
			System.out.println("folderId"+folderId);
					
		}   catch (Exception ex) {
	           ex.printStackTrace();
	        } finally {
	        	conn.close();
	        	 rs.close();
	             pstmt.close();
	        }

		return folderId;
	}
	
	public long getNextId() throws PenielException, SQLException {
        long retval = 0;
        Connection conn = null;
        PreparedStatement pstmt =null;
        ResultSet rs =null;
        
        try{
        
         conn = getDatabaseConnection();
        String sqlStatement = "select  property_seq.nextval from dual";

        System.out.println("sql" + sqlStatement);
        pstmt = conn.prepareStatement(sqlStatement);
        rs = pstmt.executeQuery();

        while (rs.next()) {
        	retval = rs.getLong(1);
        }
       
        } catch (Exception ex) {
           ex.printStackTrace();
        } finally {
        	conn.close();
        	 rs.close();
             pstmt.close();
        }

        return retval;
    }
	 
}
