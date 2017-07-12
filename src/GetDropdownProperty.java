import java.io.IOException;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.peniel.dao.DropDownDAO;

/**
 * Servlet implementation class for Servlet: GetDropDown
 *
 */
public class GetDropdownProperty extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(GetDropDown.class);

    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
    public GetDropdownProperty() {
        super();
    }

    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        System.out.println("Inside GetDropdownProperty");
        String compid = (String) request.getParameter("compid");
        String properties = getPropertyDropDown(compid);
        (response.getWriter()).write(properties);
    }

    public String getIndexDropDown(String compid) {
        String select = "select distinct index_type,index_type_id from index_type where company_id = '" + compid + "' order by index_type";
        String outputstr = "";
        String param = "index_type";
        try {
            DropDownDAO dropDownDAO = new  DropDownDAO();
            outputstr = dropDownDAO.findByQuery(param, select);
            outputstr = StringUtils.replace(outputstr, "\\\\", "'");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Error :" + e.getMessage());
        }
        return outputstr;
    }

    public String getPropertyDropDown(String compid) {
        String select = "select distinct property_id,id from property where company_id = '" + compid + "' order by property_id";
        String outputstr = "";
        String param = "property_id";
        try {
            DropDownDAO dropDownDAO = new  DropDownDAO();
            outputstr = dropDownDAO.findByQuery(param, select);
            System.out.println("outputstr " + outputstr);
            outputstr = StringUtils.replace(outputstr, "\\\\", "'");
            System.out.println("outputstr " + outputstr);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Error :" + e.getMessage());
        }
        return outputstr;
    }

    public String getDocDropDown(String compid) {
        String select = "select distinct document_type,document_type_id from document_type where company_id = '" + compid + "' order by document_type";
        String outputstr = "";
        String param = "document_type";
        try {
            DropDownDAO dropDownDAO = new  DropDownDAO();
            outputstr = dropDownDAO.findByQuery(param, select);
            outputstr = StringUtils.replace(outputstr, "\\\\", "'");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Error :" + e.getMessage());
        }
        return outputstr;
    }

    public String getDocType(String docTypeId, String compid) {
        String select = "select document_type from document_type where company_id = '" + compid + "' and document_type_id = '" + docTypeId + "' ";
        String outputstr = "";
        try {
            DropDownDAO dropDownDAO = new  DropDownDAO();
            outputstr = dropDownDAO.findByQuery("document_type1", select);
            outputstr = StringUtils.replace(outputstr, "\\\\", "'");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Error :" + e.getMessage());
        }
        return outputstr;
    }
}
