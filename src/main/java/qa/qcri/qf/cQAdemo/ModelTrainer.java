package qa.qcri.qf.cQAdemo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;

import qa.qcri.qf.semeval2015_3.textnormalization.JsoupUtils;
import it.uniroma2.sag.kelp.data.dataset.SimpleDataset;
import it.uniroma2.sag.kelp.data.example.Example;
import it.uniroma2.sag.kelp.data.example.SimpleExample;
import it.uniroma2.sag.kelp.data.label.Label;
import it.uniroma2.sag.kelp.data.label.StringLabel;
import it.uniroma2.sag.kelp.learningalgorithm.classification.liblinear.LibLinearLearningAlgorithm;
import it.uniroma2.sag.kelp.predictionfunction.classifier.BinaryLinearClassifier;
import it.uniroma2.sag.kelp.predictionfunction.classifier.BinaryMarginClassifierOutput;
import it.uniroma2.sag.kelp.utils.JacksonSerializerWrapper;
import it.uniroma2.sag.kelp.utils.JacksonSerializerWrapper1;
import it.uniroma2.sag.kelp.utils.ObjectSerializer;
import it.uniroma2.sag.kelp.utils.evaluation.BinaryClassificationEvaluator;

/**
 * 
 * 
 * @author gmartino
 *
 */

public class ModelTrainer {

	private static final boolean COMPUTE_FEATURES = true;
	private static final String CQA_QL_TRAIN_EN = "semeval2015-3/data/"
			+ "SemEval2015-Task3-English-data/datasets/emnlp15/CQA-QL-train.xml";
	private static final String CQA_QL_TEST_EN = "semeval2015-3/data/"
			+ "SemEval2015-Task3-English-data/datasets/emnlp15/CQA-QL-test.xml";
	private static final String TRAIN_FILENAME = "semeval2015-3/data/"
			//+ "SemEval2015-Task3-English-data/datasets/emnlp15/CQA-QL-train.xml.csv.klp";
			+ "SemEval2015-Task3-English-data/datasets/emnlp15/CQA-QL-train.xml.klp";
	private static final String TEST_FILENAME = "semeval2015-3/data/"
			+ "SemEval2015-Task3-English-data/datasets/emnlp15/CQA-QL-test.xml.klp";
	private static final String MODEL_FILE_NAME = TRAIN_FILENAME + ".model";
	
	private static final String VECTORIAL_LINEARIZATION_NAME = "features";
	private static final String POSITIVE_CLASS_NAME = "GOOD";
	private static final float CP = 1;
	private static final float CN = 1;
	private BinaryLinearClassifier model;
	//private DenseVectorFromListOfDouble fv;
	private SparseVectorFromListOfDouble fv;
	private StringLabel positiveClass;
	
	public ModelTrainer() {
		this.model = null;
		//this.fv = new DenseVectorFromListOfDouble();
		this.fv = new SparseVectorFromListOfDouble();
		this.positiveClass = new StringLabel(POSITIVE_CLASS_NAME);
	}

	public ModelTrainer(String positiveClass) {
		this.model = null;
		this.fv = new SparseVectorFromListOfDouble();
		this.positiveClass = new StringLabel(positiveClass);
	}

	public String getModelFileName() {
		return MODEL_FILE_NAME;
	}
		
	/**
	 * Saves a model in Kelp format to file MODEL_FILENAME 
	 * @throws IOException
	 */
	public void saveModelToFile() throws IOException {
		if (this.model==null) {
			System.out.println("ERROR: model is null, thus " + 
					"it cannot be saved on file");
			System.exit(1);
		}
		ObjectSerializer serializer = new JacksonSerializerWrapper();
		serializer.writeValueOnFile(model, MODEL_FILE_NAME);
	}
	

	/**
	 * Loads a model in Kelp format from file MODEL_FILENAME.    
	 * @return true if the model has been successfully loaded, false otherwise  
	 */
	public boolean loadModelFromURL(URL url) {
		JacksonSerializerWrapper1 serializer = new JacksonSerializerWrapper1();
		try {
			this.model = serializer.readValue(url, BinaryLinearClassifier.class);
		}catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Loads a model in Kelp format from file MODEL_FILENAME.    
	 * @return true if the model has been successfully loaded, false otherwise  
	 */
	public boolean loadModelFromFile(String modelFileName) {
		ObjectSerializer serializer = new JacksonSerializerWrapper();
		File file = new File(modelFileName);
		try {
			this.model = serializer.readValue(file, BinaryLinearClassifier.class);
		}catch (Exception e) {
			return false;
		}
		return true;
	}

	public BinaryMarginClassifierOutput getExamplePrediction(Example e) {
		return model.predict(e);
	}
	
	public Float getExampleScore(Example e) {
		return model.predict(e).getScore(positiveClass);
	}
	
	private String getRepresentationNameFromModel() {
		return VECTORIAL_LINEARIZATION_NAME;
	}
	
	/**
	 * Performs binary classification learning on TRAIN_FILENAME dataset. 
	 * Learning uses liblinear and it is influenced by CP and CN (weight
	 * for positive and negative examples, respectively).
	 * The model is then saved to file (see saveModelToFile())   
	 * @throws Exception
	 */
	private void trainSystem(String trainFileName) throws Exception {

		if (COMPUTE_FEATURES) {
			CommentSelectionDatasetCreator featureMapper = new CommentSelectionDatasetCreator();
			Document docTrain = JsoupUtils.getDoc(CQA_QL_TRAIN_EN);
			featureMapper.processEnglishFile(docTrain, CQA_QL_TRAIN_EN, "train");
			trainFileName = CQA_QL_TRAIN_EN + ".klp";
		}
		SimpleDataset trainingSet = new SimpleDataset();
		trainingSet.populate(trainFileName);
		for (Label l : trainingSet.getClassificationLabels()) {
			System.out.println("Training Label " + l.toString() + " "
					+ trainingSet.getNumberOfPositiveExamples(l));
			System.out.println("Training Label " + l.toString() + " "
					+ trainingSet.getNumberOfNegativeExamples(l));
		}
		LibLinearLearningAlgorithm liblinear = 
				new LibLinearLearningAlgorithm(positiveClass, CP,CN,
						VECTORIAL_LINEARIZATION_NAME);
		liblinear.learn(trainingSet);
		model = (BinaryLinearClassifier) liblinear.getPredictionFunction();
		saveModelToFile(); 
	}
	
	/**
	 * Classifies a test set using current model (which is loaded if necessary). 
	 * @return the classification accuracy
	 * @throws Exception
	 */
	private float classifyTestSet(String modelFileName, String testFileName) throws Exception {
		
		if (COMPUTE_FEATURES) {
			CommentSelectionDatasetCreator featureMapper = new CommentSelectionDatasetCreator();
			Document docTrain = JsoupUtils.getDoc(CQA_QL_TEST_EN);
			featureMapper.processEnglishFile(docTrain, CQA_QL_TEST_EN, "test");
			testFileName = CQA_QL_TEST_EN + ".klp";
		}
		SimpleDataset testSet = new SimpleDataset();
		if (model==null) {
			loadModelFromFile(modelFileName);
		}
		testSet.populate(testFileName);
		BinaryClassificationEvaluator evaluator = 
				new BinaryClassificationEvaluator(positiveClass);
		for (Example e : testSet.getExamples()) {
			BinaryMarginClassifierOutput predict = model.predict(e);
			evaluator.addCount(e, predict);
		}
		return evaluator.getAccuracy();
	}

	
	
	public float getExampleScoreFromFeatureVector(List<Double> featureValues) {
		
		SimpleExample ex = new SimpleExample();
		float score;
		ex.addRepresentation(getRepresentationNameFromModel(), 
				fv.createKelpSparseVectorFromArray(featureValues));
				//fv.createKelpDenseVectorFromArray(featureValues));
		return model.predict(ex).getScore(positiveClass);
		
	}
	
	public static void main(String[] args) throws Exception {

		ModelTrainer trainer = new ModelTrainer();
		System.out.println("Training system..."); //add more info
		trainer.trainSystem(TRAIN_FILENAME);
		System.out.println("Done");
		//System.out.println(trainer.classifyTestSet(MODEL_FILE_NAME, TEST_FILENAME));
	}

}

