package beast.evolution.substitutionmodel;

import java.util.ArrayList;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.alignment.CognateSet;
import beast.evolution.alignment.Language;
import beast.evolution.datatype.DataType;
import beast.evolution.tree.Node;
import beast.util.Randomizer;

/*
 * ExplicitBinaryStochasticDollo Object 
 * 
 * This Stochastic-Dollo model is defined for Languages, and works only on binary data.
 * The Binary DataType is NOT used in this class. 
 * 
 * @author Stuart Bradley (sbra886@aucklanduni.ac.nz)
 * @version 1.0
 * 
 * Need to remember 
 * to grow later languages
 * when birth events occur
 */
@Description("Binary Stochastic-Dollo model for Languages with recorded mutation events")
public class ExplicitBinaryStochasticDollo extends SubstitutionModel.Base {
	/** Backward and forward substitution rates. */
	public Input<Double> rate01Input = new Input<Double>("rate01", "substitution rate for 0 to 1 (birth), default = 0.5");
	public Input<Double> rate10Input = new Input<Double>("rate10", "substitution rate for 1 to 0 (death), default = 0.5");
	
	/** Birth and Death rates */ 
	protected double b,d;
	
	public ExplicitBinaryStochasticDollo(double birth, double death) {
		this.b = birth;
		this.d = death;
	}
	
	/*
	 * Checks birth and death rates are acceptable.
	 * @see beast.evolution.substitutionmodel.SubstitutionModel.Base#initAndValidate()
	 * @exception checks rates are below 1.
	 */
	@Override
	public void initAndValidate() throws Exception {
		double b = rate01Input.get();
		double d = rate10Input.get();
		
		if (b > 1 || d > 1) {
			throw new Exception("Rates should be equal to or below 1.");
		}
	}
	
	/*
	 * Mutates a language according to the Stochastic Dollo model.
	 * @param l Language to mutate.
	 * @param c CognateSet - contains the current max length of languages.
	 * @param T Mutation time.
	 * @return newLang Mutated language.
	 */
	public Language mutate_SD(Language l, CognateSet c ,double T) {
		Language newLang = new Language(l.getLanguage());
    	double[] probs = new double[2];
    	// Checks whether births have occurred elsewhere in the tree - and adds dead (0) traits accordingly.
    	if (c.getStolloLength() > newLang.getLanguage().size()) {
    		ArrayList<Integer> curr_seq = newLang.getLanguage();
    		for (int i = 0; i < c.getStolloLength() - newLang.getLanguage().size(); i++) {
        		curr_seq.add(0);
    		}
    		newLang.setLanguage(curr_seq); 
    	}
    	// Mutation proper.
    	double t = Randomizer.nextExponential(b+d*newLang.getBirths());
    	while (t < T) { 
    		// Set probabilities for language.
        	probs[0] = d*newLang.getBirths()/(b+d*newLang.getBirths());
        	probs[1] = b/(b+d*newLang.getBirths());
        	// If death.
        	if (Randomizer.randomChoice(probs) == 0) {
        		int randomNum;
        		int max = newLang.getLanguage().size();
        		// Find random alive trait, and kill it.
        		while (true) {
        			randomNum = Randomizer.nextInt(max);
        			if (newLang.getLanguage().get(randomNum) != 0) {
        				newLang.getLanguage().set(randomNum, 0);
        				break;
        			}
        		}
        	// Else birth.
        	} else {
        		ArrayList<Integer> curr_seq = newLang.getLanguage();
        		curr_seq.add(1);
        		newLang.setLanguage(curr_seq);
        		// Increase the number of cognate classes for later languages.
        		c.setStolloLength(c.getStolloLength() + 1);
        	}
        	t += Randomizer.nextExponential(b+d*newLang.getBirths());
        	// Record mutation.
        	l.addMutation(t, newLang.getLanguage());
    	}
    	return newLang;
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
