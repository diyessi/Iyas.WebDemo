package qa.qcri.qf.cQAdemo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * A simple class that includes all the information related to a Question and 
 * its comments in the Semeval 2015 task 3 on answer selection.
 * <br>
 * 
 * The class implements comparable in order to allow for the creation of a 
 * TreeSet of Questions. The questions are sorted by QID
 * 
 * TODO check that the sorting is actually working. 
 * TODO Junit
 * 
 * @author albarron
 *
 */
public class Question implements Comparable<Question>,  Serializable {
	
	private static final long serialVersionUID = 7155250601309427542L;
	
	public static final String THANKS = "thanks";
	public static final String NOTHANKS = "nothanks";
	public static final String HASQ = "hasq";
	public static final String NOHASQ = "nohasq";
	
	public static final String NOT_ENGLISH = "Not English"; 
	public static final String GOOD = "Good"; 
	public static final String POTENTIAL = "Potential"; 
	public static final String DIALOGUE = "Dialogue"; 
	public static final String BAD = "Bad";
	
	/** Unique, global, id of the question */
	private String qid;
		
	/** Category (Advice and help, Computers and Internet, etc.) */
	private String qcategory;
	
	/** Date when the question was generated */
	private String qdate;
	
	/** User ID for the "questioner" Q */
	private String quserid;
	
	/** Type of question (GENERAL, YES_NO) */
	private String qtype;
	
	/** ??? */
	private String qgold_yn;		
	
	
	/** Subject of the question, as set by Q */
	private String qsubject;
	
	/** Body of the question */
	private String qbody;
		
	/** Ordered list of the comments; potential answers to the question */
	private List<Comment> comments;
	
	
	/**
	 * Invoke the class without setting any value. Comments list is initialized.
	 */
	public Question(){
		comments = new ArrayList<Comment>();
	}
	
	/**
	 * Invoke the class setting all the values for the question (but not the 
	 * comments).
	 *  
	 * @param qid
	 * @param qcategory
	 * @param qdate
	 * @param quserid
	 * @param qtype
	 * @param qgold_yn
	 * @param qsubject
	 * @param qbody
	 */
	public Question(String qid, String qcategory, String qdate, String quserid,
			String qtype, String qgold_yn, String qsubject, String qbody) {
		this();
		setQid(qid);		
		setQcategory(qcategory);
		setQdate(qdate);
		setQuserId(quserid);
		setQtype(qtype);
		setQgoldYN(qgold_yn);		
		setQsubject(qsubject);
		setQbody(qbody);		
	}
	
	
	/**
	 * Add a new comment
	 * @param cid comment id: (includes QID)
	 * @param cuserid	user id
	 * @param cGold	Not English, good, ...
	 * @param cGoldYN Whether it is a Y/N answer
	 * @param cSubject	Subject of the comment
	 * @param cBody	Comment
	 */
	public void addComment(String cid, String cuserid, String cGold, String cGoldYN, 
							String cSubject, String cBody){
		comments.add(new Comment(cid, cuserid, cGold, cGoldYN, 
							cSubject, cBody));		
	}	
	
	
	public void addComment(Comment comment){
		comments.add((Comment)comment);		
	}	

        /**
	 * A simple class that stores all the values of a Question-related comment.
	 * 
	 * 
	 * TODO put it into another file?
	 * 
	 * @author albarron
	 *
	 */
	public class Comment implements Serializable{
			
		private static final long serialVersionUID = 6131037150458266024L;

		/** comment id: (includes QID) */
		private String cid;
	
		/** user id */
		private String cuserid;
		
		/** Not English, good, ... */
		private String cgold;
		
		/** Whether it is a Y/N answer */
		private String cgold_yn;
		
		/** Subject of the comment */
		private String csubject;
		
		/** Comment */
		private String cbody;		
		
		public Comment(){
                    this.cid = "";
                    this.cuserid = "";
                    this.cgold = "";
                    this.cgold_yn = "";
                    this.csubject = "";
                    this.cbody = "";
		}

                public Comment(String cid, String cuserid, String cGold, String cGoldYN, 
						String cSubject, String cBody){
			
			this.cid = cid;
			this.cuserid = cuserid;
			this.cgold = cGold;
			this.cgold_yn = cGoldYN;
			this.csubject = cSubject;
			this.cbody = cBody;
		}

                public void setCid(String id){
                        cid = id;		
                }

                public void setCuserid(String userid){
                        cuserid = userid;		
                }

                public void setCgold(String gold){
                        cgold = gold;		
                }
                
                public void setCgold_yn(String gold_yn){
                        cgold_yn = gold_yn;		
                }

                public void setCsubject(String subject){
                        csubject = subject;		
                }

                public void setCbody(String body){
                        cbody = body;		
                }
                
                
                public String getCid() {
			return cid;
		}

		public String getCuserid() {
			return cuserid;
		}

		public String getCgold() {
			return cgold;
		}

		public String getCgold_yn() {
			return cgold_yn;
		}

		public String getCsubject() {
			return csubject;
		}

		public String getCbody() {
			return cbody;
		}
		
		public void setGold(String cGold){
			this.cgold = cGold;
		}

                public String toString() {
                    String s;
                    
                    s = String.format("cid:%s\ncuserid:%s\ncGold:%s\ncGoldYN:%s\ncSubject:%s\ncBody:%s\n", cid, cuserid, cgold, cgold_yn, csubject, cbody);

                    return s;
                }	
	}

	
	public boolean hasGoodComments(){
		for (Comment comment : comments){
			if (comment.cgold.equals(GOOD))
				return true;
		}
		return false;
	}
	
	public boolean hasGoodCommentsByQ(){
		for (Comment comment : comments) {
			if (comment.cgold.equals(GOOD) &&
				comment.cuserid.equals(getQuserid()))
					return true;			
		}
		return false;		
	}	
	
	/* Getters */
	
	public String getQid(){
		return qid;
	}
	
	public List<Comment> getComments(){
		return comments;
	}
	
	/**
	 * @return number of comments associated to this question
	 */
	public int size(){
		return comments.size();
	}
	
	public Map<Integer, Comment> getCommentsOfType(String type){		
		Map<Integer, Comment> mComments = new TreeMap<Integer, Comment>();		
		List<Comment> lComments = getComments();
		Iterator<Comment> it = lComments.iterator();
		int i=1;
	    for (Comment comment : lComments) {	        	        
	        if (comment.getCgold().equals(type)) {
	        	mComments.put(i, comment);
	        }
	        i++;        
	    }
		return mComments;		
	}
	
	
	public Map<Integer, Comment> getCommentsByQ(String type){
		boolean remove = false;
		Map<Integer, Comment> qComments = getCommentsByQ();
		
		Iterator it = qComments.entrySet().iterator();
	    while (it.hasNext()) {
	    	remove = false;
	        Map.Entry pairs = (Entry) it.next();
	        switch(type){
				case THANKS:
					//FIXME Hamdy: remove = (! FeatureExtractor.containsAcknowledge(
			        		//(Comment) pairs.getValue())) ? true : false;
					break;
				case NOTHANKS:
					//FIXME Hamdy: remove = (FeatureExtractor.containsAcknowledge(
			        		//(Comment) pairs.getValue())) ? true : false;
					break;
				case HASQ:
					//FIXME Hamdy: remove = (! FeatureExtractor.containsQuestion((
			        		//(Comment) pairs.getValue()))) ? true : false;
					break;
				case NOHASQ: 	
					//FIXME Hamdy: remove = (FeatureExtractor.containsQuestion((
			        		//(Comment) pairs.getValue()))) ? true : false;
					break;				
	        }	        
	        if (remove){
	        	it.remove();
	        }	        
	    }
		return qComments;
	}
		
	public Map<Integer, Comment> getCommentsByQThanks(){
		Map<Integer, Comment> qComments = getCommentsByQ();
		
		Iterator it = qComments.entrySet().iterator();
	    while (it.hasNext()) {	    	
	        Map.Entry pairs = (Entry) it.next();
	        //FIXME Hamdy: if (! FeatureExtractor.containsAcknowledge(
	        		//(Comment) pairs.getValue())){
	        	//it.remove();
	        //}	        
	    }
	    return qComments;		 
	}
	
	public Map<Integer, Comment> getCommentsByQNoThanks(){
		Map<Integer, Comment> qComments = getCommentsByQ();
		
		Iterator it = qComments.entrySet().iterator();
	    while (it.hasNext()) {	    	
	        Map.Entry pairs = (Entry) it.next();
	        //FIXME Hamdy: if (FeatureExtractor.containsAcknowledge(
	        		//(Comment) pairs.getValue())){
	        	//it.remove();
	        //}	        
	    }
	    return qComments;		 
	}
	
	
	public Map<Integer, Comment> getCommentsByQ(){		
		Map<Integer, Comment> qComments = new TreeMap<Integer, Comment>();
		int i =1;
		for (Comment comment : comments){			
			if (comment.cuserid.equals(getQuserid())){
				qComments.put(i, comment);
			}
			i++;			
		}
		
		return qComments;
		
	}
	
	public String getQcategory() {
		return qcategory;
	}
	
	public String getQdate() {
		return qdate;
	}
	
	public String getQuserid() {
		return quserid;
	}
	
	public String getQtype() {
		return qtype;
	}
	
	public String getQgold_yn() {
		return qgold_yn;		
	}
	
	public String getQsubject() {
		return qsubject;
	}
	
	public String getQbody() {
		return qbody;
	}
	
	public int getNumberOfComments(){
		return comments.size();
	}
	
	public int getNumberOfCommentsType(String type){
		int i = 0;
		for (Comment comment : comments){
			if (comment.cgold.equals(type)){
				i++;
			}			
		}
		return i;
	}
	
	/* Setters */
	
	public void setQid(String id){
		qid = id;		
	}
	
	public void setQcategory(String category){
		qcategory = category;
	}
	
	public void setQdate(String date){
		qdate = date;
	}
	
	public void setQuserId(String user){
		quserid = user;
	}
	
	public void setQtype(String type){
		qtype = type;
		
	}

	public void setQgoldYN(String yn){
		qgold_yn = yn;		
	}
	
	public void setQsubject(String subject){
		qsubject = subject;
	}

	public void setQbody(String body){
		qbody = body;
	}
	
	

	private int getMethodToSort(){
		return Integer.valueOf(getQid().replaceAll("[a-zA-Z]", ""));
	}

	@Override
	public int compareTo(Question o) {
		if (getMethodToSort() > o.getMethodToSort()) {
		      return 1;
		    } else if (getMethodToSort() < o.getMethodToSort()) {
		      return -1;
		    }  
		    return 0;
	}

        public String toString() {
            int i, n;
            String s;
            s = String.format("qid:%s\nqcategory:%s\nqdate:%s\nquserid:%s\nqtype:%s\nqgold_yn:%s\nqsubject:%s\nqbody:%s\n", qid, qcategory, qdate, quserid, qtype, qgold_yn, qsubject, qbody);
            
            n = comments.size();
            for (i = 0; i < n; i++){
                s += String.format("----------\nComment[%d/%d]: %s", i + 1, n, comments.get(i));
            }
            return s;
        }	
}
