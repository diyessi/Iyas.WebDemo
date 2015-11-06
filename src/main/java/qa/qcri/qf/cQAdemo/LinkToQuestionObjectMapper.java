package qa.qcri.qf.cQAdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import qa.qcri.qf.semeval2015_3.textnormalization.JsoupUtils;
import qa.qf.qcri.cqa.CQAinstance;
import qa.qf.qcri.cqa.CQAquestion;

/**
 * A class to create Question objects from QatarLiving.com urls. 
 * 
 *
 */

public class LinkToQuestionObjectMapper {
	
	public LinkToQuestionObjectMapper() {
		// TODO by Hamdy
	}

	/**
	 * The function, for each url of the QatarLiving website, create a Question object from the corresponding thread  
	 * @param urls, a list of String representing urls 
	 * @return a list of Question objects, each one related to the corresponding url 
	 * @throws IOException 
	 */
	public List<CQAinstance> getQuestions(List<String> urls) throws IOException {

		List<CQAinstance> candidateAnswers = new ArrayList<CQAinstance>();
		Document doc = JsoupUtils.getDoc("semeval2015-3/data/SemEval2015-Task3-English-data/datasets/trialSearchResult.xml");
		for (Element thread : doc.getElementsByTag("Question")) {
			candidateAnswers.add(new CQAinstance(thread));
		}

		return candidateAnswers;
	}
	
	
	/**
   * Function for testing purposes 
   * @return a list of Question objects 
   */
  public List<CQAinstance> getQuestions() {
    List<CQAinstance> qList = new ArrayList<CQAinstance>();
    String qid = "1234";
    String qcategory = "";
    String qdate = "";
    String quserid = "";
    String qtype = "";  // Type of question (GENERAL, YES_NO)
    String qgold_yn = ""; //{Yes, No}  
    String qsubject = ""; //question subject
    String qbody = "";    //question body
    
    // FIXME ABC Sep 14, 2015. Gio: I changed from Question to CQAquestion.
    // As you may remember, it has not category, as that is assigned to CQAinstance.
    // IF you really need it
    // a) we can switch this object to CQAinstnace, without comments
    // b) we can move category down to the question
    //Having said so, I believe you actually want to return CQAinstances here, don't you?
    // Now that you read all of this, forget it. I realised you need to return CQAinstance
    //BUT I assume you still don't add comments here 'cause this is what you mentioned: either
    //creating a toy instance or not... In that case, I guess you need a for???
    
    CQAquestion q = new CQAquestion(qid, qdate, quserid, qtype, qgold_yn, qsubject, qbody); 
    //new Question(qid, qcategory, qdate, quserid, qtype, qgold_yn, qsubject, qbody);
    CQAinstance cqainstance = new CQAinstance(q, qcategory);
    qList.add(cqainstance);
    return qList;
    
  }
	
//  /**
//   * The function, for each url of the QatarLiving website, create a Question object from the corresponding thread  
//   * @param urls, a list of String representing urls 
//   * @return a list of Question objects, each one related to the corresponding url 
//   */
//  public List<Question> getQuestions(List<String> urls) {
//    return null;
//  }
//  
//  /**
//   * Function for testing purposes 
//   * @return a list of Question objects 
//   */
//  public List<Question> getQuestions() {
//    List<Question> qList = new ArrayList<Question>();
//    String qid = "1234";
//    String qcategory = "";
//    String qdate = "";
//    String quserid = "";
//    String qtype = "";  // Type of question (GENERAL, YES_NO)
//    String qgold_yn = ""; //{Yes, No}  
//    String qsubject = ""; //question subject
//    String qbody = "";    //question body
//    Question q = new Question(qid, qcategory, qdate, quserid, qtype, qgold_yn, qsubject, qbody);
//    qList.add(q);
//    return qList;
//  }
	
}
