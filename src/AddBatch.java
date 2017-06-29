import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.peniel.bus.vo.BatchUpload;
import com.peniel.bus.vo.Company;
import com.peniel.dao.BatchUploadDAO;
import com.peniel.dao.CompanyDAO;
import com.peniel.dao.DocumentDataDAO;
import com.peniel.utilities.FileNameProcessor;
import com.peniel.utilities.ParseMultiForm;
import com.peniel.utilities.ToPDF;
import com.penielsolutions.converter.Converter;
import com.penielsolutions.converter.ConverterService;
import com.penielsolutions.converter.ConverterServiceLocator;


public class AddBatch extends HttpServlet {

	 private static Logger logger = Logger.getLogger(AddBatch.class);
	public AddBatch() {
		super();
	}

	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	    doPost(request, response);
	}

	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println("In AddBatch doPost ");
        String fileName = "";
        String uploadtype = "";
        String uploadDate = "";
        String description = "";
        String success = "";
        String successAfterInsert = "";
        
        String cId = "";
        String sessionid = "";
        String confirmParam = "";
        String strResponse = "";
        String companyName = "";
        String fileUploadSize = "";  
        String FileSize = "";
        
        ParseMultiForm pMF = new ParseMultiForm(request);
        StringBuffer g = new StringBuffer(strResponse);
        String param;
        String batchId = "";
        String uId = "";

        String batchFolder = "";
        try
        {
        	javax.naming.Context ctx = new javax.naming.InitialContext();
        	batchFolder = (String) ctx.lookup("java:comp/env/batchfolder");
			
        } catch(Exception e)
        {
        	logger.debug("Exception retrieving config param batchfolder -->"+e.getMessage());
        }
        while ( (param = pMF.getNextParameter(g)) != null )
        {

            if ("file".equals(param)) 
            {

                System.out.println("A file param detected");
                String infile = pMF.getFilename();                

                if (infile != null) {
                    String outfilename = null;
                    try {
                        System.out.println("Company Name ---> " + companyName);
                        BatchUploadDAO batchUploadDAO = new BatchUploadDAO();
                                        
                        switch (FileNameProcessor.fileType(pMF.getFilename())) 
                        {
                            
                            case FileNameProcessor.ZIP_FILE:
                            {
                            	
                               batchId = Long.toString(batchUploadDAO.getNextId1());
                          	   fileName = batchId+"_" +uploadDate+ ".zip";
                               outfilename = buildDestinationFileName(companyName, batchFolder, fileName);
                               saveFile(pMF, outfilename);
                                                           	     
                            	System.out.println("fileName ---> "+fileName);
                               
                                System.out.println("outfilename "+outfilename);                             
                                
                                /* delete the uploaded file and the conversion file
                                System.out.println("Deleting old file ");
                                File x = new File(fileName);
                                x.delete();*/
                                
                                /*try{
                                	javax.naming.Context ctx = new javax.naming.InitialContext();
                                	fileUploadSize = (String) ctx.lookup("java:comp/env/fileUploadSize");
                                	File inputFile = new File(fileName);
                                	System.out.println("Checking file size...........size=" + inputFile.length());
                                	
                                	if(inputFile.length()  > Long.parseLong(fileUploadSize))
                                	{
                                		System.out.println("File Size Error"+fileName);
			                            success = "error";
			                            
	                                	// file size too large
	                                	// delete the uploaded file 
	                                    logger.info("Deleting  file " + fileName);
	                                    File y = new File(fileName);
	                                    y.delete();
	                                    
	                                	logger.info("Too large file" + fileName);
	                                	success = "error";
	                                	FileSize = "error_lagre_file_size";                                		
                                	}                               		
                        			
                                } catch(Exception e){
                                	logger.debug("Exception retrieving config param fileUploadSize -->"+e.getMessage());
                                	success = "error";
                                }*/ 
                            }
                            break;
                           
                            default:
                                throw new Exception("Upload file type is invalid");
                        }
                        if(!success.equalsIgnoreCase("error"))
                        {
	                        strResponse = strResponse + " +++Uploading " + fileName;
	                        strResponse = strResponse + " +++Uploaded to " + outfilename;
	
	                        success = "yes";
                        }
                    } catch (Exception e) 
                    {                        
                        success = "error";
                        e.printStackTrace();
                        System.out.println("error in upbean store -->" + e.getMessage());
                    }
                }

            } 
            else 
            {
                String k = param;

                String v = "";

                if (k.equalsIgnoreCase("message")) 
                {
                    v = pMF.getMessage();
                } 
                else 
                {
                    v = pMF.getParameter();

                    if (k.equalsIgnoreCase("sessionid")) 
                    {
                        sessionid = v;
                    }
                    if (k.compareToIgnoreCase("fileName") == 0) 
                    {
                        fileName = v;
                    }

                 
                    if (k.compareToIgnoreCase("uploadDate") == 0) 
                    {
                    	
                    	uploadDate = v;
                    	System.out.println("Upload Date ---> "+uploadDate);
                    }
                    

                    if ("uId".equals(k)) 
                    {
                        uId = v;
                        System.out.print("uId --->"+uId);
                    }
                    if ("description".equals(k)) 
                    {
                    	description = v;
                        System.out.print("description --->"+uId);
                    }
                    if ("cid".equals(k)) 
                    {
                        cId = v;
                        
                        CompanyDAO companyDao = new CompanyDAO();
                        Vector companyList = new Vector();
                        Company company = new Company();

                        companyList = companyDao.findById(cId);
                        company = (Company) companyList.get(0);
                        companyName = company.getCompanyName();
                    }
                    if ("confirmParam".equals(param)) 
                    {
                        confirmParam = v;
                    }
                    
                   

                }

                System.out.println("key = "+k+" value = "+v);
            }
        }


        request.setAttribute("sessionid", sessionid);
        request.setAttribute("fileName", fileName);
       

        System.out.println("companyName=" + companyName + "***" + "cid=" + cId + "***" + "fileName=" + fileName + "***" );
        

        

        if ("Yes".equalsIgnoreCase(confirmParam) || "Yes1".equalsIgnoreCase(confirmParam)) 
        {
        	//success = "yes";
        	successAfterInsert = "yes";

            // Add the record to the db
            System.out.println("Adding doument to DB --->");
           
            BatchUploadDAO batchUploadDAO = new BatchUploadDAO();
            try 
            {              
            	
                String webFileName = companyName + "/" + batchFolder + "/" + fileName;
                System.out.println("uId=========="+uId+" **** webFileName = "+ webFileName + " ****** cId = " + cId + " *** sessionid = " + sessionid 
                					+"\n" );
                batchUploadDAO.InsertBatchUploadData(uId,webFileName, cId, sessionid,description);
			}
            catch (Exception e) 
            {
            	successAfterInsert = "no";
            	e.printStackTrace();
                logger.info("Error :" + e.getMessage());
            	
            }
            logger.info("success after insert -->" + successAfterInsert);
				
				
            if ("Yes".equalsIgnoreCase(confirmParam)) 
            {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/addBatchResult.jsp?id=" + fileName + "&success=" + success + "&successAfterInsert=" + successAfterInsert );
                dispatcher.forward(request, response);
            } else if ("Yes1".equalsIgnoreCase(confirmParam)) 
            {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/addBatch.jsp?id=" + fileName + "&success=" + success +"&successAfterInsert=" + successAfterInsert);
                dispatcher.forward(request, response);
            }
        }
        else 
        {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/addBatch1.jsp?id=" + fileName + "&success=" + success +"&FileSize=" + FileSize);
            dispatcher.forward(request, response);
        }
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}
	public String buildDestinationFileName(String companyName, String batchFolder, String infile) {
	    String retval =  getDestinationBaseDirectory() + File.separator + companyName + File.separator + batchFolder + File.separator + infile;
	    System.out.println("Destination with company name Directory " + retval);
	    return retval;
	}
	public String getDestinationBaseDirectory() {
	    return  this.getServletContext().getRealPath("scaimages");
	}
	 public void saveFile(ParseMultiForm pMF, String outfilename) throws IOException {
	        FileOutputStream OutFile = new FileOutputStream(outfilename);

	        // Copies the file from
	        pMF.getParameter(OutFile);
	        OutFile.flush();
	        OutFile.close();
	        System.out.println("File saved from "+OutFile);
	    }

}



