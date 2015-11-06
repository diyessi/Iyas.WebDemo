package qa.qcri.qf.cQAdemo;

import it.uniroma2.sag.kelp.data.example.SimpleExample;
import it.uniroma2.sag.kelp.data.representation.vector.DenseVector;
import it.uniroma2.sag.kelp.data.representation.vector.SparseVector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public class DenseVectorFromListOfDouble extends DenseVector {
	
	public DenseVectorFromListOfDouble() {
		super();
	}

	public DenseVector createKelpDenseVectorFromArray(List<Double> featureValues) {
		
		double[] featureArray = ArrayUtils.toPrimitive(featureValues.toArray(
				new Double[featureValues.size()]));
		DenseVector dv = new DenseVector(featureArray);
		return dv;
	}
	
	/*
	private SparseVector createKelpSparseVectorFromArray(
			ArrayList<Double> featureValues) {
		
		SparseVector sp = new SparseVector();
		int i = 0; 
		for (double entry : featureValues) {
			i = i+1;
			sp.setFeatureValue(Integer.toString(i), (float) entry);
		}
		return sp;
	}
	*/
	
	private SimpleExample createKelpExampleFromVector(
			ArrayList<Double> featureValues, String representationName) {
		
		SimpleExample ex = new SimpleExample();
		ex.addRepresentation(representationName, 
				createKelpDenseVectorFromArray(featureValues));
		//ex.addRepresentation(representationName, 
		//		createKelpSparseVectorFromArray(featureValues));
		return ex;		
	}

}
