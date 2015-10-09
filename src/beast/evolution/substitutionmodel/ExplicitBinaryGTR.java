package beast.evolution.substitutionmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.alignment.CognateSet;
import beast.evolution.alignment.Language;
import beast.evolution.datatype.Binary;
import beast.evolution.datatype.DataType;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
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
public class ExplicitBinaryGTR  extends LanguageSubsitutionModel {
	/** Backward and forward substitution rates. */
	public Input<Double> rateInput = new Input<Double>("rate01", "substitution rate, default = 0.5");
	
	/** Binary rate matrix */
	protected double rate;
	
	public ExplicitBinaryGTR(double r) {
		this.rate = r;
	}
	
	/*
	 * Creates full rate matrix from input.
	 * @see beast.evolution.substitutionmodel.SubstitutionModel.Base#initAndValidate()
	 * @exception checks rates are below 1.
	 */
	@Override
	public void initAndValidate() {
		this.rate = rateInput.get();
	}
	
	/*
	 * Mutates a language according to the GTR model.
	 * @param l Initial Language
	 * @param T total mutation time
	 * @return newLang mutated Language
	 */
	public Language mutateLang(Language l, CognateSet c, double T) {
		ArrayList<Integer> s = new ArrayList<Integer>(l.getLanguage());
        Language newLang = new Language(s);
        for (int i = 0; i < newLang.getLanguage().size(); i++) {
        	int currentTrait = newLang.getLanguage().get(i);
        	// Mutations are exponentially distributed.
        	double t = Randomizer.nextExponential(rate);
        	while (t < T) {
        		currentTrait = newLang.getLanguage().get(i);
        		// In binary model, a mutation switches trait.
        		newLang.getLanguage().set(i, 1 - currentTrait);
        		// Record mutation event in old language.
        		l.addMutation(t, newLang.getLanguage());
        		t += Randomizer.nextExponential(rate);
        	}
        }
        return newLang;
	}
	
	/*
	 * Mutates down a already generated tree.
	 * @param base Tree with starting language in root.
	 * @param c CognateSet, gets updated as languages are created.
	 * @return base Tree with languages added.
	 */
	public Tree mutateOverTree(Tree base, CognateSet c) {
		ArrayList<Node> currParents = new ArrayList<Node>();
		ArrayList<Node> newParents = new ArrayList<Node>();
		currParents.add(base.getRoot());
		while (currParents.size() > 0) {
			for (Node parent : currParents) {
				List<Node> children = parent.getChildren();
				for (Node child : children) {
					double T = child.getHeight() - parent.getHeight();
					Language parentLang = (Language) parent.getMetaData("lang");
					Language newLang = mutateLang(parentLang, c, T);
					child.setMetaData("lang", newLang);
					c.addLanguage(newLang);
					newParents.add(child);
				}
			}
			currParents = new ArrayList<Node>(newParents);
			newParents = new ArrayList<Node>();
		}
		return base;
	}

	@Override
	public Tree mutateOverTreeBorrowing(Tree base, CognateSet c, Double borrow, Double z) {
		// TODO Auto-generated method stub
		return null;
	}
}
