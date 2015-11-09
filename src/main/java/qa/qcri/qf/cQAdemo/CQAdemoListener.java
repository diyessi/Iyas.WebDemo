package qa.qcri.qf.cQAdemo;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.uima.UIMAException;

import qa.qf.qcri.cqa.CQAinstance;

/**
 * Application Lifecycle Listener implementation class CQAdemoListener
 *
 */
@WebListener
public class CQAdemoListener implements ServletContextListener {
	static final String MODEL_FILE_NAME = "/data/"
			+ "SemEval2015-Task3-English-data/datasets/emnlp15/" 
			+ "CQA-QL-train.xml.klp.model";
	
	static private Demo demo;

	static synchronized public List<CQAinstance> getQuestionAnswers(String userQuestion) throws UIMAException, IOException{
		return demo.getQuestionAnswers(userQuestion);
	}

    /**
     * Default constructor. 
     */
    public CQAdemoListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  {
    	// We could set up a pool of demo objects do that have getQuestionAnswers take an unused one from the pool.
    	// This can't run for too long or it will time out, so it would be better to trigger a background task setting up the Demo object(s).
		try {
			demo = new Demo();
			demo.loadModel(MODEL_FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  { 
    	demo = null;
    }
	
}
