package qa.qcri.qf.cQAdemo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.uima.UIMAException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;

import qa.qf.qcri.cqa.CQAinstance;

/**
 * Servlet implementation class CQArelated
 */
@WebServlet(name = "CQArelated", description = "Returns related questions and their ranked comments", urlPatterns = { "/CQArelated" })
public class CQArelated extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CQArelated() {
        super();
        // TODO Auto-generated constructor stub
    }

    static int getParameterInt(HttpServletRequest request, String parameter, int defaultValue){
    	String parameterValue = request.getParameter(parameter);
    	try {
    		return Integer.parseInt(parameterValue);
    	} catch(NumberFormatException e){
    		
    	}
    	return defaultValue;
    }
    
    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String jsonp = request.getParameter("jsonp");
		String text = request.getParameter("text");
		int maxHits = getParameterInt(request, "maxHits", 15);
		int maxComments = getParameterInt(request, "maxComments", 20);
		
		try {
			List<CQAinstance> threads = CQAdemoListener.getQuestionAnswers(text, maxHits, maxComments);
			ObjectMapper objectMapper = new ObjectMapper();
			response.setContentType("application/javascript");
			PrintWriter writer = response.getWriter();
			objectMapper.writeValue(writer, null == jsonp ? threads : new JSONPObject(jsonp, threads));
		} catch (UIMAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
