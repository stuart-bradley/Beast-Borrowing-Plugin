package beast.evolution.substitutionmodel;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.alignment.Language;
import beast.evolution.datatype.DataType;
import beast.evolution.tree.Node;
import beast.util.Randomizer;

/*
 * ExplicitGTRModel Object 
 * 
 * This GTR model is defined for Languages, and works only on binary data.
 * The Binary DataType is NOT used in this class. 
 * 
 * @author Stuart Bradley (sbra886@aucklanduni.ac.nz)
 * @version 1.0
 * 
 * Keeping things binary
 * Allows for cool little shortcuts
 * i is i - 1
 */
@Description("Binary GTR Model for Languages with recorded mutation events")
public class ExplicitBinaryGTR  extends SubstitutionModel.Base {
	/** Backward and forward substitution rates. */
	public Input<Double> rate01Input = new Input<Double>("rate01", "substitution rate for 0 to 1 (birth), default = 0.5");
	public Input<Double> rate10Input = new Input<Double>("rate10", "substitution rate for 1 to 0 (death), default = 0.5");
	
	/** Binary rate matrix */
	protected double[] rateMatrix = new double[4];
	
	/*
	 * Creates full rate matrix from input.
	 * @see beast.evolution.substitutionmodel.SubstitutionModel.Base#initAndValidate()
	 * @exception checks rates are below 1.
	 */
	@Override
	public void initAndValidate() throws Exception {
		double rate01 = rate01Input.get();
		double rate10 = rate10Input.get();
		
		if (rate01 > 1 || rate10 > 1) {
			throw new Exception("Rates should be equal to or below 1.");
		}
		rateMatrix[0] = rate01 * -1.0;
		rateMatrix[1] = rate01;
		rateMatrix[2] = rate10;
		rateMatrix[3] = rate10 * -1.0;
	}
	
	/*
	 * Mutates a language according to the GTR model.
	 * @param l Initial Language
	 * @param T total mutation time
	 * @return newLang mutated Language
	 */
	public Language mutate_GTR(Language l, double T) {
        Language newLang = new Language(l.getLanguage());
        for (int i = 0; i < newLang.getLanguage().size(); i++) {
        	int currentTrait = l.getLanguage().get(i);
        	double rate = getRate(currentTrait);
        	// Mutations are exponentially distributed.
        	double t = Randomizer.nextExponential(-rate);
        	while (t < T) {
        		// In binary model, a mutation switches trait.
        		newLang.getLanguage().set(i, 1 - currentTrait);
        		// Record mutation event in old language.
        		l.addMutation(t, newLang.getLanguage());
        	}
        }
        return newLang;
	}
	
	/*
	 * Returns (negative) mutation rate.
	 * @param currentTrait 0 or 1 
	 * @return (negative) rate from rateMatrix
	 */
	private double getRate(int currentTrait) {
		double rate;
		if (currentTrait == 0) {
    		rate = rateMatrix[0];
    	} else {
    		rate = rateMatrix[3];
    	}
		return rate;
	}
	
	/*
	 * Returns nothing, because mutations are explicit. 
	 * @see beast.evolution.substitutionmodel.SubstitutionModel#getTransitionProbabilities(beast.evolution.tree.Node, double, double, double, double[])
	 */
	@Override
	public void getTransitionProbabilities(Node node, double fStartTime, double fEndTime, double fRate,
			double[] matrix) {	
	}
	
	/*
	 * No EigenDecomposition is required.
	 * @see beast.evolution.substitutionmodel.SubstitutionModel#getEigenDecomposition(beast.evolution.tree.Node)
	 */
	@Override
	public EigenDecomposition getEigenDecomposition(Node node) {
		return null;
	}
	
	/*
	 * TO-DO: Figure out what the hell this does.
	 * @see beast.evolution.substitutionmodel.SubstitutionModel#canHandleDataType(beast.evolution.datatype.DataType)
	 */
	@Override
	public boolean canHandleDataType(DataType dataType) {
		return true;
	}
}
