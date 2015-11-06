package qa.qcri.qf.cQAdemo;

import it.uniroma2.sag.kelp.data.example.SimpleExample;
import it.uniroma2.sag.kelp.data.representation.vector.DenseVector;
import it.uniroma2.sag.kelp.data.representation.vector.SparseVector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public class SparseVectorFromListOfDouble extends SparseVector {
	
	public SparseVectorFromListOfDouble() {
		super();
	}
	
	public SparseVector createKelpSparseVectorFromArray(
			List<Double> featureValues) {
		
		SparseVector sp = new SparseVector();
		int i = 0; 
		for (double entry : featureValues) {
			i = i+1;
			sp.setFeatureValue(Integer.toString(i), (float) entry);
		}
		return sp;
	}
	
/*	
	private SimpleExample createKelpExampleFromVector(
			ArrayList<Double> featureValues, String representationName) {
		
		SimpleExample ex = new SimpleExample();
		ex.addRepresentation(representationName, 
				createKelpSparseVectorFromArray(featureValues));
		//ex.addRepresentation(representationName, 
		//		createKelpSparseVectorFromArray(featureValues));
		return ex;		
	}
*/
}
