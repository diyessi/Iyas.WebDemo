
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.uima.UIMAException;

import qa.qcri.qf.cQAdemo.CommentSelectionDatasetCreator;
import qa.qcri.qf.cQAdemo.Demo;
import qa.qcri.qf.cQAdemo.ModelTrainer;
import qa.qcri.qf.cQAdemo.QatarLivingURLMapping;
import qa.qcri.qf.cQAdemo.QuestionRetriever;
import qa.qf.qcri.cqa.CQAcomment;
import qa.qf.qcri.cqa.CQAinstance;


/**
 * Servlet implementation class DemoServlet
 */
@WebServlet("/CQAdemoApp")
public class CQAdemoApp extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
//	private static String MODEL_FILE_NAME = "semeval2015-3/data/"
//			+ "SemEval2015-Task3-English-data/datasets/emnlp15/" 
//			+ "CQA-QL-train.xml.klp.model";
	private static String MODEL_FILE_NAME = "/data/"
			+ "SemEval2015-Task3-English-data/datasets/emnlp15/" 
			+ "CQA-QL-train.xml.klp.model";
	private String fileBasePath;
	private Demo demo; 
	private ServletConfig config;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CQAdemoApp() {
        super();
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Load here the data structures or in the constructor?
		this.config = config;
		fileBasePath = config.getServletContext().getRealPath(File.separator);
		initDemo();
	}
	 
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */ 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter() ;
		out.println("<!DOCTYPE html>"
				+ "<html>"
				+ "<head>"
				+ "<meta charset=\"UTF-8\">"
						+ "<title>CQA Demo</title>"
						+ "</head>"
						+ "<body>"
						+ "<p>"
						+ "How Can I Help You Today?"
						+ "</p>"
						+ "<form action=\"/CQAdemoApp/CQAdemoApp\" method=\"post\">"
								+ "Type a question <input type=\"text\" name=\"userQuestion\">"
								+ "<input type=\"submit\" value=\"submit\">"
								+ "</form>"
								+ "</body>"
								+ "</html>");
        out.close() ;		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String userQuestion = request.getParameter("userQuestion");
		List<CQAinstance> threads;
		PrintWriter writer = response.getWriter();
		try {
			threads = demo.getQuestionAnswers(userQuestion);
			printOnCommandLine(writer, userQuestion, threads);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			printOnCommandLine(writer, userQuestion, null);
			e.printStackTrace();
		} catch (UIMAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printOnCommandLine(PrintWriter out, 
			String userQuestion, List<CQAinstance> threadList) {
		
		List<CQAcomment> clist;

		out.println("<html>") ;
        out.println("<head><title>Qatar Living</title></head>") ;
        out.println("<body>") ;
        out.println("Question: <h1>" + userQuestion + "</h1>") ;
		out.println("<ul>");
		for (CQAinstance thread : threadList) {
			clist = thread.getComments();
			Collections.sort(clist);
			out.println("<li>Thread: " 
					+ thread.getQuestion().getWholeText());
			out.println("<ul>");
			for (CQAcomment c : clist) {
				out.println("<li>"+c.getWholeText()+" ("+c.getScore()+")</li>");
			}
			out.println("</ul>");
		}
		out.println("</ul>");		
        out.println("</body>") ;
        out.println("</html>") ;
        out.close() ;
	}
	
	/**
	 * 
	 */
	private void initDemo() {
		try {
			demo = new Demo();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!demo.loadModel(MODEL_FILE_NAME)) {	
			System.out.println("Error: cannot load model from file: " 
					+ MODEL_FILE_NAME);
			System.exit(1);
		}					
		//load map files?
	}
	
}
