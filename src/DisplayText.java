import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;

public class DisplayText extends HttpServlet {

    /**
	 * Constructor of the object.
	 */
    public DisplayText() {
        super();
    }

    /**
	 * Destruction of the servlet. <br>
	 */
    public void destroy() {
        // Just puts "destroy" string in log
        super.destroy();
    // Put your code here
    }

    /**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String filepath = request.getParameter("id");
        String logofull;
        try {
            logofull = getFromEnv("repository");
            logofull = logofull + java.io.File.separator.toString() + filepath;
            ServletOutputStream out = response.getOutputStream();
            System.out.println("file full path " + logofull);
            String ext = FilenameUtils.getExtension(logofull);
            /*  if(ext.equalsIgnoreCase("doc") || ext.equalsIgnoreCase("docx") || ext.equalsIgnoreCase("xlsx") || ext.equalsIgnoreCase("xls"))
		  {
		*/
            System.out.println("ext doc");
            if (logofull == null || logofull.equals(""))
                throw new ServletException("Invalid or non-existent file parameter in SendWord servlet.");
            ServletOutputStream stream = null;
            BufferedInputStream buf = null;
            try {
                stream = response.getOutputStream();
                File doc = new File(logofull);
                //set response headers
                response.setContentType("MediaType.APPLICATION_OCTET_STREAM");
                response.addHeader("Content-Disposition", "attachment; filename=" + doc.getName());
                response.setContentLength((int) doc.length());
                FileInputStream input = new FileInputStream(doc);
                buf = new BufferedInputStream(input);
                int readBytes = 0;
                while ((readBytes = buf.read()) != -1) stream.write(readBytes);
            } catch (IOException ioe) {
                throw new ServletException(ioe.getMessage());
            } finally {
                if (stream != null)
                    stream.close();
                if (buf != null)
                    buf.close();
            }
        //	}  
        } catch (NamingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
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

    public String getFromEnv(String val) throws NamingException {
        javax.naming.Context ctx = new javax.naming.InitialContext();
        return (String) ctx.lookup("java:comp/env/" + val);
    }
}
