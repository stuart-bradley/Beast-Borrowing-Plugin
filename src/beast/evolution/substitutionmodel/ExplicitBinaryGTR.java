package beast.evolution.substitutionmodel;

import java.util.ArrayList;
import java.util.List;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.alignment.Sequence;
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
public class ExplicitBinaryGTR extends LanguageSubsitutionModel {
	/** Backward and forward substitution rates. */
	public Input<Double> rateInput = new Input<Double>("rate", "substitution rate, default = 0.5");

	/** Binary rate matrix */
	protected double rate;
	

	public ExplicitBinaryGTR() throws Exception {
	}

	public ExplicitBinaryGTR(double r) {
		this.rate = r;
	}

	/*
	 * Creates full rate matrix from input.
	 * 
	 * @see
	 * beast.evolution.substitutionmodel.SubstitutionModel.Base#initAndValidate(
	 * )
	 */
	@Override
	public void initAndValidate() {
		this.rate = rateInput.get();
	}

	/*
	 * Mutates a language according to the GTR model.
	 * 
	 * @param l Initial Language
	 * 
	 * @param T total mutation time
	 * 
	 * @return newLang mutated Language
	 */
	public Sequence mutateLang(Sequence l, double T) throws Exception {
		Sequence newLang = new Sequence("", l.getData());
		for (int i = 0; i < newLang.getData().length(); i++) {
			int currentTrait = (int) newLang.getData().charAt(i);
			// Mutations are exponentially distributed.
			double t = Randomizer.nextExponential(rate);
			while (t < T) {
				currentTrait =  Character.getNumericValue(newLang.getData().charAt(i));
				// In binary model, a mutation switches trait.
				String newSeq = replaceCharAt(newLang.getData(), i, Integer.toString((1 - currentTrait)));
				newLang.dataInput.setValue(newSeq, newLang);
				t += Randomizer.nextExponential(rate);
			}
		}
		return newLang;
	}

	/*
	 * Mutates down a already generated tree.
	 * 
	 * @param base Tree with starting language in root.
	 * 
	 * @return base Tree with languages added.
	 */
	public Tree mutateOverTree(Tree base) throws Exception {
		ArrayList<Node> currParents = new ArrayList<Node>();
		ArrayList<Node> newParents = new ArrayList<Node>();
		currParents.add(base.getRoot());
		while (currParents.size() > 0) {
			for (Node parent : currParents) {
				List<Node> children = parent.getChildren();
				for (Node child : children) {
					double T = Math.abs(child.getHeight() - parent.getHeight());
					Sequence parentLang = getSequence(parent);
					Sequence newLang = mutateLang(parentLang, T);
					child.setMetaData("lang", newLang);
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
	 * 
	 * @param base Tree with starting language in root.
	 * 
	 * @param borrow borrowing rate.
	 * 
	 * @param z local borrowing rate, 0.0 rate implies global borrowing.
	 * 
	 * @return base Tree with languages added.
	 */
	public Tree mutateOverTreeBorrowing(Tree base, Double borrow, Double z) throws Exception {
		Double treeHeight = getTreeHeight(base);
		// Get root node.
		ArrayList<Node> aliveNodes = getAliveNodes(base, 0.0);
		// Get first event.
		Double totalRate = totalRate(aliveNodes, borrow);
		Double t = Randomizer.nextExponential(totalRate);
		// Variable declarations.
		Node ranNode = null, ranNode2 = null;
		Sequence nodeLang = null, nodeLang2 = null, newNodeLang;
		String s;
		int idx;
		double[] probs;
		while (t < treeHeight) {
			// Return array of event probabilities and pick one.
			probs = BorrowingProbs(aliveNodes, borrow);
			Integer choice = Randomizer.randomChoicePDF(probs);
			switch (choice) {
			// Mutate.
			case 0:
				// Pick a random node at time t.
				idx = Randomizer.nextInt(aliveNodes.size());
				ranNode = aliveNodes.get(idx);
				nodeLang = getSequence(ranNode);
				// Pick a random position in language.
				int pos = Randomizer.nextInt(nodeLang.getData().length());
				int currentTrait =  Character.getNumericValue(nodeLang.getData().charAt(pos));
				// Flip the bit at the random position.
				s = replaceCharAt(nodeLang.getData(), pos, Integer.toString((1 - currentTrait)));
				newNodeLang = new Sequence("",s);
				newNodeLang.dataInput.setValue(s, newNodeLang);
				setSubTreeLanguages(ranNode, newNodeLang);
				break;
			// Borrow.
			case 1:
				// Borrowing only occurs if there are multiple languages.
				if (aliveNodes.size() < 2) {
					break;
				}
				// Pick two distinct languages at random.
				while (true) {
					idx = Randomizer.nextInt(aliveNodes.size());
					ranNode = aliveNodes.get(idx);
					nodeLang = getSequence(ranNode);
					idx = Randomizer.nextInt(aliveNodes.size());
					ranNode2 = aliveNodes.get(idx);
					nodeLang2 = getSequence(ranNode2);
					if (ranNode != ranNode2) {
						break;
					}
				}
				// Check they're close enough together.
				if (localDist(ranNode, ranNode2, z) == false) {
					break;
				} else {
					// Randomly iterate through language and find a 1.
					for (Integer i : getRandLangIndex(nodeLang)) {
						if (Character.getNumericValue(nodeLang.getData().charAt(i)) == 1) {
							// Give the 1 to the receiving language.
							s = replaceCharAt(nodeLang2.getData(), i, Integer.toString(1));
							newNodeLang = new Sequence("",s);
							newNodeLang.dataInput.setValue(s, newNodeLang);
							setSubTreeLanguages(ranNode2, newNodeLang);
							break;
						}
					}
				}
				break;
			}
			aliveNodes = getAliveNodes(base, t);
			totalRate = totalRate(aliveNodes, borrow);
			t += Randomizer.nextExponential(totalRate);
		}
		return base;
	}

	/*
	 * Probabilities for different events.
	 * 
	 * @param aliveNodes, see aliveNodes(base, t).
	 * 
	 * @param borrow borrowing rate.
	 * 
	 * @return double[], array of probabilities.
	 */
	protected double[] BorrowingProbs(ArrayList<Node> aliveNodes, Double borrow) throws Exception {
		Double totalRate = totalRate(aliveNodes, borrow);
		Double borrowSum = 0.0;
		Double mutateSum = 0.0;
		for (Node n : aliveNodes) {
			borrowSum += getBirths(getSequence(n));
			mutateSum += (getSequence(n)).getData().length();
		}
		double[] probs = new double[2];
		probs[0] = rate * mutateSum / totalRate;
		probs[1] = borrow * rate * borrowSum / totalRate;
		return probs;
	}

	/*
	 * Total rate of mutation.
	 * 
	 * @param aliveNodes, see aliveNodes(base, t).
	 * 
	 * @param borrow borrowing rate.
	 * 
	 * @return Double, total rate,
	 */
	protected Double totalRate(ArrayList<Node> aliveNodes, Double borrow) throws Exception {
		Double borrowSum = 0.0;
		Double mutateSum = 0.0;
		for (Node n : aliveNodes) {
			borrowSum += getBirths(getSequence(n));
			mutateSum += (getSequence(n)).getData().length();
		}
		return rate * mutateSum + borrow * rate * borrowSum;
	}
}
