package beast.evolution.substitutionmodel;

import java.util.ArrayList;
import java.util.List;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.alignment.CognateSet;
import beast.evolution.alignment.Language;
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

	/*
	 * Mutates down a tree, includes global and local borrowing.
	 * @param base Tree with starting language in root.
	 * @param c CognateSet gets updated at the end, once all languages are created.
	 * @param borrow borrowing rate.
	 * @param z local borrowing rate, 0.0 rate implies global borrowing. 
	 * @return base Tree with languages added. 
	 */
	public Tree mutateOverTreeBorrowing(Tree base, CognateSet c, Double borrow, Double z) {
		Double treeHeight = getTreeHeight(base);
	    ArrayList<Node> aliveNodes = getAliveNodes(base, 0.0);
	    Double totalRate = totalRate(aliveNodes, borrow);
    	Double t = Randomizer.nextExponential(totalRate);
    	Node ranNode, ranNode2;
    	Language nodeLang, nodeLang2 ,newNodeLang;
    	ArrayList<Integer> s;
    	int idx;
    	double[] probs;
    	while (t < treeHeight) {
    		probs = BorrowingProbs(aliveNodes, borrow);
    		Integer choice = Randomizer.randomChoice(probs);
    		switch(choice) {
    		// Mutate.
    		case 0: 
    			idx = Randomizer.nextInt(aliveNodes.size());
    			ranNode = aliveNodes.get(idx);
    			nodeLang = (Language) ranNode.getMetaData("lang");
    			int pos = Randomizer.nextInt(nodeLang.getLanguage().size());
				s = new ArrayList<Integer>(nodeLang.getLanguage());
		        newNodeLang = new Language(s);
		        int currentTrait = newNodeLang.getLanguage().get(pos);
		        newNodeLang.getLanguage().set(pos, 1 - currentTrait);
		        setSubTreeLanguages(ranNode, newNodeLang);
    			break;
    		// Borrow.
    		case 1:
    			idx = Randomizer.nextInt(aliveNodes.size());
    			ranNode = aliveNodes.get(idx);
    			nodeLang = (Language) ranNode.getMetaData("lang");
    			idx = Randomizer.nextInt(aliveNodes.size());
    			ranNode2 = aliveNodes.get(idx);
    			nodeLang2 = (Language) ranNode2.getMetaData("lang");
    			if (nodeLang.getLanguage() == nodeLang2.getLanguage()) {
    				break;
    			} else if (localDist(ranNode, ranNode2, z) == false) {
    				break;
    			} else {
    				for (Integer i : getRandLangIndex(nodeLang)) {
    					try {
    					if (nodeLang.getLanguage().get(i) == 1 && nodeLang2.getLanguage().get(i) == 0) {
    						s = new ArrayList<Integer>(nodeLang2.getLanguage());
            		        newNodeLang = new Language(s);
            		        newNodeLang.getLanguage().set(i, 1);
            		        setSubTreeLanguages(ranNode2, newNodeLang);
    						break;
    					}
    					} catch (IndexOutOfBoundsException e) {
    						continue;
    					}
    				}
    			}
    			break;
    		}
    		aliveNodes = getAliveNodes(base, t);
    		t += Randomizer.nextExponential(totalRate);
    	}
		return base;
	}
	
	/*
	 * Probabilities for different events.
	 * @param aliveNodes, see aliveNodes(base, t).
	 * @param borrow borrowing rate.
	 * @return double[], array of probabilities.
	 */
	protected double[] BorrowingProbs(ArrayList<Node> aliveNodes, Double borrow) {
		Double totalRate = totalRate(aliveNodes, borrow);
		Double borrowSum = 0.0;
		for (Node n : aliveNodes) {
			borrowSum += ((Language) n.getMetaData("lang")).getBirths();
		}
    	double[] probs = new double[2];
    	probs[0] = rate/totalRate;
    	probs[1] = borrow*borrowSum/totalRate;
    	return probs;
	}
	
	/*
	 * Total rate of mutation.
	 * @param aliveNodes, see aliveNodes(base, t).
	 * @param borrow borrowing rate.
	 * @return Double, total rate,
	 */
	protected Double totalRate(ArrayList<Node> aliveNodes, Double borrow) {
		Double borrowSum = 0.0;
		for (Node n : aliveNodes) {
			borrowSum += ((Language) n.getMetaData("lang")).getBirths();
		}
		return rate*rate*borrow*borrowSum;
	}
}
