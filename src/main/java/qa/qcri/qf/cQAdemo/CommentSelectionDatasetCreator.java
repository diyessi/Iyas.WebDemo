package qa.qcri.qf.cQAdemo;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import qa.qcri.qf.semeval2015_3.FeatureExtractor;
import qa.qcri.qf.semeval2015_3.PairFeatureFactoryEnglish;
import qa.qcri.qf.semeval2015_3.textnormalization.TextNormalizer;
import qa.qcri.qf.trees.TreeSerializer;
import qa.qf.qcri.cqa.CQAabstractElement;
import qa.qf.qcri.cqa.CQAcomment;
import qa.qf.qcri.cqa.CQAinstance;
import util.Stopwords;
import cc.mallet.types.AugmentableFeatureVector;

import com.google.common.base.Joiner;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.similarity.algorithms.api.SimilarityException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class CommentSelectionDatasetCreator 
    extends qa.qcri.qf.emnlp2015.CommentSelectionDatasetCreatorV2{

  /** */
  public static final boolean WRITE_FEATURES_TO_FILE = false;

  /** Add the meaning */
  private boolean firstRow;
  
  //TODO ABC, SEP 9. Confirm why these variables are global (but not defined)
  //TODO ABC, SEP 10. Why do we need this global if it is locally defined in 
  //processEnglishFile?
  //private JCas questionCas; //added by Giovanni
  private String suffix; //added by Giovanni
  private TreeSerializer ts; //added by Giovanni
  private String plainTextOutputPath;//added by Giovanni
  private String goodVSbadOutputPath;//added by Giovanni
  private String pairwiseOutputPath;//added by Giovanni
  private String kelpFilePath;//added by Giovanni

  public CommentSelectionDatasetCreator() throws UIMAException, IOException {
    super();
    this.firstRow = true;
  }

  public CommentSelectionDatasetCreator(Stopwords stopwords) throws UIMAException, IOException {
    super(stopwords);
    this.firstRow = true;
  }
  
  public static void main(String[] args) throws IOException, UIMAException, 
      SimilarityException {
    /**
     * Setup logger
     */
    org.apache.log4j.BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(Level.INFO);
    
    /**
     * Run the code for the English tasks
     */
    new CommentSelectionDatasetCreator().runForEnglish();
  }

  
  
//  /**
//   * TODO Gio: please document this 
//   * @param thread
//   * @return
//   * @throws UIMAException
//   * @throws IOException
//   */
//  public ArrayList<Double> getCommentFeatureRepresentation(Element thread) 
//      throws UIMAException, IOException {
//    return this.getCommentFeatureRepresentation(thread, 
//        thread.getElementsByTag("QBody").get(0).text());
//  }
  
//  public ArrayList<Double> getCommentFeatureRepresentation(Element thread, String userQuestion) 
//      throws IOException, UIMAException {
//    
//    /*Comment-level features to be combined*/
//    List<List<Double>>  listFeatures = new ArrayList<List<Double>>();
//    
//    /** Parse question node */
//    String qid = thread.attr("QID");
//    String qcategory = thread.attr("QCATEGORY");
//    String qdate = thread.attr("QDATE");
//    String quserid = thread.attr("QUSERID");
//    String qtype = thread.attr("QTYPE");
//    String qgold_yn = thread.attr("QGOLD_YN");    
//    String qsubject = JsoupUtils.recoverOriginalText(thread.getElementsByTag("QSubject").get(0).text());
//    qsubject = TextNormalizer.normalize(qsubject);
//    String qbody = thread.getElementsByTag("QBody").get(0).text();
//    qbody = JsoupUtils.recoverOriginalText(UserProfile.removeSignature(qbody, userProfiles.get(quserid)));
//    qbody = TextNormalizer.normalize(qbody);
//    //      questionCategories.add(qcategory);
//    CQAabstractElement q = new CQAquestion(qid, qdate, quserid, qtype, qgold_yn, qsubject, qbody);
//    
//    Elements comments = thread.getElementsByTag("Comment");
//    for (Element comment : comments) {
//      String cid = comment.attr("CID");
//      String cuserid = comment.attr("CUSERID");
//      String cgold = comment.attr("CGOLD");
//      //Replacing the labels for the "macro" ones: GOOD vs BAD
//      if (ONLY_BAD_AND_GOOD_CLASSES){
//        if (cgold.equalsIgnoreCase("good")){
//          cgold = GOOD;
//        } else {
//          cgold = BAD;
//        }
//      }
//      String cgold_yn = comment.attr("CGOLD_YN");
//      String csubject = JsoupUtils.recoverOriginalText(comment.getElementsByTag("CSubject").get(0).text());
//      csubject = TextNormalizer.normalize(csubject);
//      String cbody = comment.getElementsByTag("CBody").get(0).text();
//      cbody = JsoupUtils.recoverOriginalText(UserProfile.removeSignature(cbody, userProfiles.get(cuserid)));
//      cbody = TextNormalizer.normalize(cbody);
//      
//      q.addComment(cid, cuserid, cgold, cgold_yn, csubject, cbody);
//    }
//    return this.getCommentFeatureRepresentation(q, userQuestion);
//  }
  
  	class PipelineTask extends ForkJoinTask<Boolean> {
  		JCas jCas;
  		final AnalysisEngine[] analysisEngineList;
  		
  		PipelineTask() throws ResourceInitializationException{
  			analysisEngineList = new AnalysisEngine[]{
  	  				createEngine(createEngineDescription(OpenNlpSegmenter.class)),
  	  			    createEngine(createEngineDescription(OpenNlpPosTagger.class)),
  	  			    createEngine(createEngineDescription(OpenNlpChunker.class)),
  	  			    createEngine(createEngineDescription(StanfordLemmatizer.class))
  	  		};
  		}

		@Override
		public Boolean getRawResult() {
			return false;
		}

		@Override
		protected void setRawResult(Boolean value) {
		}

		@Override
		protected boolean exec() {
			try {
				SimplePipeline.runPipeline(jCas, analysisEngineList);
				return true;
			} catch (AnalysisEngineProcessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		public JCas getJCas() {
			return jCas;
		}
		
		public void setJCas(JCas jCas){
			this.jCas = jCas;
			reinitialize();
		}
  	}
  	
  	class QuestionTask extends PipelineTask {
  		CQAabstractElement question;
  		
  		QuestionTask() throws UIMAException{
  			super();
  		}
  		
  		public void setQuestion(CQAabstractElement question) throws UIMAException{
  			setJCas(cqaElementToCas(question));
  			this.question = question;
  		}

		public CQAabstractElement getQuestion() {
			return question;
		}
  	}
  	QuestionTask questionTask = new QuestionTask();
  	List<Map<String, Double>> albertoSimoneFeatures;
  	class CommentTask extends PipelineTask {
  		CQAcomment comment;
  		AugmentableFeatureVector fv;
  		int commentIndex;
  		
  		CommentTask() throws UIMAException{
  			super();
  		}
  		
		@Override
		protected boolean exec() {
			super.exec();
			System.out.println("Massimo features");
			if (GENERATE_MASSIMO_FEATURES) {
				synchronized (pfEnglish) {
					fv = (AugmentableFeatureVector) pfEnglish.getPairFeatures(questionTask.getJCas(), jCas,
							PARAMETER_LIST);
				}
			} else {
				fv = new AugmentableFeatureVector(alphabet);
			}
			if (GENERATE_ALBERTO_AND_SIMONE_FEATURES) {
				System.out.println("Alberto and Simone features");
				Map<String, Double> featureVector = albertoSimoneFeatures.get(commentIndex);

				for (String featureName : FeatureExtractor.getAllFeatureNames()) {
					Double value = featureVector.get(featureName);
					double featureValue = 0;
					if (value != null) {
						featureValue = value;
					}
					fv.add(featureName, featureValue);
				}

			}

			return true;
		}
  		
  		public void setComment(CQAcomment comment) throws UIMAException{
  			setJCas(cqaElementToCas(comment));
  			this.comment = comment;
  		}
  		
		public CQAcomment getComment() {
			return comment;
		}

		public AugmentableFeatureVector getFv() {
			return fv;
		}

		public int getCommentIndex() {
			return commentIndex;
		}

		public void setCommentIndex(int commentIndex) {
			this.commentIndex = commentIndex;
		}
  	}
  	Stack<CommentTask> commentTaskPool = new Stack<>();
  	
  	public CommentTask getCommentTask(CQAcomment comment, int commentIndex) throws UIMAException{
  		CommentTask commentTask = commentTaskPool.isEmpty() ? new CommentTask() : commentTaskPool.pop();
  		commentTask.setComment(comment);
  		commentTask.setCommentIndex(commentIndex);
  		return commentTask;
  	}
  	
  	public void returnCommentTask(CommentTask commentTask){
  		commentTaskPool.push(commentTask);
  	}
  
  //TODO This should be a List<List<Double>> as it contains all the features for the entire thread
  public List<List<Double>> getCommentFeatureRepresentation(CQAinstance thread) throws IOException, UIMAException {
	//String userQuestion = thread.getQuestion().getBody();
    
    //ArrayList<Double> featureMap = new ArrayList<Double>();

    /*Comment-level features to be combined*/
    List<List<Double>>  listFeatures = new ArrayList<List<Double>>();
    
    // TODO ABC, Sep 14 2015. Check why, if CQAinstance is the input, we try to loead again??
    // maybe I made a mistake in the refactoring??
    //Question q = new Question();
    /**
     * Parse question node
     */
    //String qid = thread.getQid();
    //String quserid = thread.getQuserid();
//    String qsubject = TextNormalizer.normalize(thread.getQuestion().getSubject());
//    String qbody = thread.getQuestion().getBody(); 
//    qbody = JsoupUtils.recoverOriginalText(
//        UserProfile.removeSignature(qbody, userProfiles.get(thread.getQuestion().getUserId())));
//    qbody = TextNormalizer.normalize(qbody);
//    //      questionCategories.add(qcategory);
//  //FIXME ABC Sep 14 2015. There's a problem here and a reason why this normalization
//    //should be carried out IN THE CLASS: now we cannot compute this!!!
//    String questionText = SubjectBodyAggregator.getQuestionText(qsubject, qbody);
    /**
     * Setup question CAS
     */
    System.out.println("Loading the cas");
    //JCas questionCas = cqaElementToCas(thread.getQuestion());
//    this.questionCas.reset();
//    this.questionCas.setDocumentLanguage("en");  
//    //fm.writeLn(plainTextOutputPath, "---------------------------- QID: " + qid + " USER:" + quserid);
//    //fm.writeLn(plainTextOutputPath, questionText);
//    this.questionCas.setDocumentText(thread.getQuestion().getWholeTextNormalized(userProfiles));

    /** Run the UIMA pipeline */
    questionTask.setQuestion(thread.getQuestion());
    ForkJoinPool.commonPool().submit(questionTask);
    JCas questionCas = questionTask.getJCas();

    //SimplePipeline.runPipeline(questionCas, this.analysisEngineList);

    //this.analyzer.analyze(questionCas, new SimpleContent("q-" + qid, qsubject + ". " + qbody));

    /** Parse comment nodes */
    //Elements comments = thread.getElementsByTag("Comment");

    /** Extracting context statistics for context Features */
    System.out.println("Mapping to two classes");
    for (CQAcomment comment : thread.getComments()) {
      if (ONLY_BAD_AND_GOOD_CLASSES){
        //String cgold = comment.getGold();
        String cgold = (comment.getGold().equalsIgnoreCase("good")) ? GOOD : BAD;
        comment.setGold(cgold);
      }
      //FIXME What do we normalize this for if we are not using it?
      //String csubject = TextNormalizer.normalize(comment.getSubject());
      //String cbody = TextNormalizer.normalize(comment.getBody());
    }

   
    
    if (GENERATE_ALBERTO_AND_SIMONE_FEATURES){
      albertoSimoneFeatures = FeatureExtractor.extractFeatures(thread);
    }

    List<JCas> allCommentsCas = new ArrayList<JCas>();
    List<CommentTask> commentTasks = new ArrayList<>();

    questionTask.join();
    System.out.println("Question pipeline complete");

    int commentIndex = 0;
    for (CQAcomment comment : thread.getComments()) {
    	CommentTask commentTask = getCommentTask(comment, commentIndex++);
    	commentTasks.add(commentTask);
    	ForkJoinPool.commonPool().submit(commentTask);
    }
    
    for (CommentTask commentTask : commentTasks) {
      JCas commentCas = commentTask.getJCas();
      CQAcomment comment = commentTask.getComment();
//      String cid = comment.getId();
//      String cuserid = comment.getUserId();
//      String cgold = comment.getGold();
//      String cgold_yn = comment.getGold_yn();
//      String csubject = comment.getSubject();
//      String cbody = comment.getBody();
//      /**
//       * Setup comment CAS
//       */
//      JCas commentCas = JCasFactory.createJCas();
//      commentCas.setDocumentLanguage("en");
//      String commentText = SubjectBodyAggregator.getCommentText(csubject, cbody);
//      commentCas.setDocumentText(commentText);
      /**
       * Run the UIMA pipeline
       */
      commentTask.join();
      System.out.println("Comment pipeline complete");
      //SimplePipeline.runPipeline(commentCas, this.analysisEngineList);
      //this.analyzer.analyze(commentCas, new SimpleContent("c-" + cid, csubject + ". " + cbody));

      System.out.println("Massimo features");
      AugmentableFeatureVector fv;
      /*
      if (GENERATE_MASSIMO_FEATURES){
        fv = (AugmentableFeatureVector) pfEnglish.getPairFeatures(questionCas, commentCas, PARAMETER_LIST);
      } else {
        fv = new AugmentableFeatureVector(this.alphabet);
      }
      */
      
      /*
      if (GENERATE_ALBERTO_AND_SIMONE_FEATURES){
          System.out.println("Alberto and Simone features");
        Map<String, Double> featureVector = albertoSimoneFeatures.get(commentIndex);
        
        for (String featureName : FeatureExtractor.getAllFeatureNames()){
          Double value = featureVector.get(featureName);
          double featureValue =0;
          if (value!=null) {
            featureValue = value;
          }
          fv.add(featureName, featureValue);
        }

      }
      commentIndex++;
      */
      fv = commentTask.getFv();

      /***************************************
       * * * * PLUG YOUR FEATURES HERE * * * *
       ***************************************/

      /**
       * fv is actually an AugmentableFeatureVector from the Mallet library
       * 
       * Internally the features are named so you must specify an unique identifier.
       * 
       * An example:
       * 
       * ((AugmentableFeatureVector) fv).add("your_super_feature_id", 42);
       * 
       * or:
       * 
       * AugmentableFeatureVector afv = (AugmentableFeatureVector) fv;
       * afv.add("your_super_feature_id", 42);
       * 
       */

      //((AugmentableFeatureVector) fv).add("quseridEqCuserid", quseridEqCuserid);

      /***************************************
       * * * * THANKS! * * * *
       ***************************************/

      /**
       * Produce output line
       */
  
      List<Double> features = this.serializeFv(fv);
      listFeatures.add(features);
      if (WRITE_FEATURES_TO_FILE) {
          System.out.println("Writing features to file");
        this.writeFeaturesToFile(fv, questionCas, commentCas, 
            thread.getQuestion().getId(), comment.getId(), comment.getGold(), comment.getGold_yn());
            //qid, cid, cgold, cgold_yn);
      }
      /**
       * Produce also the file needed to train structural models
       */     
      allCommentsCas.add(commentCas);
      returnCommentTask(commentTask);
    }
    
    if (WRITE_FEATURES_TO_FILE) {
      this.fm.write(this.pairwiseOutputPath, computePairwiseFeatures(thread, listFeatures, allCommentsCas));
      //out.writeLn(computePairwiseFeatures(q, listFeatures);
    }
    return listFeatures;
  }

  
   //FIXME ABC, Sep 10. Gio: this function is used, but returns null as 
  //setFileSuffix is never used. Either remove these two methods and make
  //suffix a parameter in the necessary method or actually use the setter at 
  //construction time
  private String getFileSuffix() {
    return this.suffix;
  }
  
  //TODO ABC, Sep 9. Gio: this is never used and returns the parameter. 
  //Is there some reason to maintain it?
  private void setFileSuffix(String suffix) {
    this.suffix = suffix;
  }
  
  private void writeFeaturesToFile(AugmentableFeatureVector fv, JCas questionCas, JCas commentCas,  
      String qid, String cid, String cgold, String cgold_yn) {
    
    List<Double> features = this.serializeFv(fv);

    if (this.firstRow) {
      //header for Good vs Bad
      this.fm.write(this.goodVSbadOutputPath, "qid,cgold,cgold_yn");
      for(int i = 0; i < fv.numLocations(); i++) {
        int featureIndex = i + 1;
        this.fm.write(this.goodVSbadOutputPath, ",f" + featureIndex);
      }
      this.fm.writeLn(this.goodVSbadOutputPath, "");
      
      //header for pairwise
      this.fm.write(this.pairwiseOutputPath, "qid,cgold");
      int numFeatures = fv.numLocations();
      if (COMBINATION_CONCAT){
        numFeatures *=2;
      }
      if (INCLUDE_SIMILARITIES){
        numFeatures += PairFeatureFactoryEnglish.NUM_SIM_FEATURES;
      }
        
      for (int i = 0; i < numFeatures; i++) {
        int featureIndex = i + 1;
        this.fm.write(this.pairwiseOutputPath, ",f" + featureIndex);
      }
      this.fm.writeLn(this.pairwiseOutputPath, "");
      this.firstRow = false;
    }
    
    this.fm.writeLn(this.goodVSbadOutputPath, 
        cid + "," + cgold + "," + cgold_yn + "," 
        + Joiner.on(",").join(features)); 
    
    if (PRODUCE_SVMLIGHTTK_DATA) {
      produceSVMLightTKExample(questionCas, commentCas, this.getFileSuffix(), this.ts,
          qid, cid, cgold, cgold_yn, features);
    }
    if (PRODUCE_KELP_DATA){
      produceKelpExample(questionCas, commentCas, this.kelpFilePath, this.ts,
          qid, cid, cgold, cgold_yn, features);
    }
    
    //return features;
  }

  
  
}
