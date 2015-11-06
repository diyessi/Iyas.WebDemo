package qa.qcri.qf.cQAdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;

import edu.stanford.nlp.util.StringUtils;
import qa.qf.qcri.cqa.CQAcomment;
import qa.qf.qcri.cqa.CQAinstance;
import util.Stopwords;

public class Demo {

	private static String MODEL_FILE_NAME = "/data/"
			+ "SemEval2015-Task3-English-data/datasets/emnlp15/" 
			+ "CQA-QL-train.xml.klp.model";

	private final int MAX_HITS =15;
	private final int MAX_COMMENTS=20;
	
	private CommentSelectionDatasetCreator featureMapper; 
	private ModelTrainer model;
	private QuestionRetriever qr;
	private QatarLivingURLMapping threadObjectBuilder;
	
	public Demo() throws IOException {
		try {
			this.featureMapper = new CommentSelectionDatasetCreator();
			this.model = new ModelTrainer();
			this.qr = new QuestionRetriever();
			//this.threadObjectBuilder = new LinkToQuestionObjectMapper();
			this.threadObjectBuilder = new QatarLivingURLMapping("/qatarliving");
		} catch (UIMAException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	public Demo(Stopwords stoplist, String fileBasePath)  throws UIMAException, IOException {
	  //this();
	  this.featureMapper = new CommentSelectionDatasetCreator(stoplist);
    this.model = new ModelTrainer();
    this.qr = new QuestionRetriever();
    //this.threadObjectBuilder = new LinkToQuestionObjectMapper();
    this.threadObjectBuilder = new QatarLivingURLMapping(fileBasePath + "resources/");
	}
	
	
	/**
	 * Retrieve a set of threads, each one containing a candidate answer to the 
	 * question typed by the user
	 * 
	 * @param userQuestion a String with the question typed by the user
	 * @return an array of Element objects, each one related to the thread 
	 * containing a candidate answer 
	 * @throws IOException 
	 */
	private List<CQAinstance> retrieveCandidateAnswers(String userQuestion) 
			throws IOException {
		
		List<CQAinstance> candidateAnswers;
		System.out.println("Retreving candidate threads");	
		candidateAnswers = threadObjectBuilder.getQuestions(qr.getLinks(userQuestion, MAX_HITS));
		if (candidateAnswers.size()==0) {
			System.out.println("No similar questions were found in QatarLiving");
			//System.exit(0); //TODO delete this line if possible
		}
		System.out.format("%d candidate threads retrieved%n", candidateAnswers.size());
		return candidateAnswers;
	}	

	
	/**
	 * getQuestionAnswers
	 *  
	 * @param userQuestion, the question in plain text
	 * @throws UIMAException
	 * @throws IOException
	 */
	public List<CQAinstance> getQuestionAnswers(String userQuestion) 
			throws UIMAException, IOException {
		List<List<Double>> threadFeatures = new ArrayList<List<Double>>();
		List<CQAinstance> threads;
		float score;
		int counter;
		
		threads = retrieveCandidateAnswers(userQuestion);
		System.out.format("Candidate threads: %s%n", threads.size());
		for(CQAinstance thread : threads) {
		  System.out.format("Processing question %s%n", thread.getQuestion().getId());
		  CQAinstance smallThread = new CQAinstance(thread.getQuestion(), thread.getQuestion().getId());
		  counter = 0;
		  for (CQAcomment com : thread.getComments()) {
		    if (counter++ > MAX_COMMENTS) {
		      break;
		    }
		    System.out.format("\tGrabbing comment %s%n", com.getId());
		    smallThread.addComment(com);
		  }
		  //threadFeatures = featureMapper.getCommentFeatureRepresentation(thread);
		  System.out.println("Computing comment feature representation");
			threadFeatures = featureMapper.getCommentFeatureRepresentation(smallThread);
			for (int i=0; i<smallThread.getNumberOfComments(); i++) {
			  System.out.format("\tGetting score for comment %d%n", i);
				score = model.getExampleScoreFromFeatureVector(threadFeatures.get(i));
				smallThread.getComment(i).setPrediction("", score); 
			}
		}
		System.out.format("Related questions retrieved: %d%n", threads.size());
		return threads;
	}
	
	public boolean loadModel(String modelFileName) {
		return this.model.loadModelFromURL(this.getClass().getResource(modelFileName));
	}
	
	
	
	public static void main(String[] args) throws Exception {
		long begin = System.currentTimeMillis();
		org.apache.log4j.BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		String userQuestion;
		
		Demo demo = new Demo();
				
		if (!demo.loadModel(MODEL_FILE_NAME)) {	
			System.out.println("Error: cannot load model from file: " 
					+ MODEL_FILE_NAME);
			System.exit(1);
		}			
		//write on the log file from which file the model has been loaded
		
		if(args.length > 0) {
			userQuestion = StringUtils.join(args, " ");
		}else{
			System.out.println("Asking a deafult question");
			userQuestion = "is there any temple in Qatar?";
		}
		System.out.println("Processing question: " + userQuestion);
		
		List<CQAinstance> threads = demo.getQuestionAnswers(userQuestion);

		OutputVisualization out = new OutputVisualization(threads);
		System.out.println("User Question: " + userQuestion);
		out.printOnCommandLine();
		System.out.println("Done");
		System.out.println("TIME: " + (System.currentTimeMillis() - begin));
		
	}

}

