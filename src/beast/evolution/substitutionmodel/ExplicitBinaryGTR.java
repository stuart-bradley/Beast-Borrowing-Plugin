package beast.evolution.substitutionmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
	public Input<Double> rateInput = new Input<Double>("rate", "substitution rate");

	/** rate */
	protected double rate;

	public ExplicitBinaryGTR() throws Exception {
	}

	public ExplicitBinaryGTR(double r, double b, double z, boolean e) {
		this.setRate(r);
		this.setBorrowRate(b);
		this.setBorrowZ(z);
		this.setNoEmptyTrait(e);
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
		this.borrowRate = borrowInput.get();
		this.borrowZ = borrowZInput.get();
		this.noEmptyTrait = noEmptyTraitInput.get();
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
			String newSeq;
			while (t < T) {
				currentTrait = Character.getNumericValue(newLang.getData().charAt(i));
				// In binary model, a mutation switches trait.
				// If death: check NoEmptyTrait.
				if (1 - currentTrait == 0) {
					if (noEmptyTraitCheck(newLang)) {
						newSeq = replaceCharAt(newLang.getData(), i, Integer.toString((1 - currentTrait)));
					} else {
						newSeq = newLang.getData();
					}
				} else {
					newSeq = replaceCharAt(newLang.getData(), i, Integer.toString((1 - currentTrait)));
				}
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
	public Tree mutateOverTreeBorrowing(Tree base) throws Exception {
		Double[] events = getEvents(base);
		setSubTreeLanguages(base.getRoot(), (Sequence) base.getRoot().getMetaData("lang"));
		// Get root node.
		ArrayList<Node> aliveNodes = new ArrayList<Node>();
		aliveNodes.addAll(base.getRoot().getChildren());
		// Get first event.
		Double totalRate = totalRate(aliveNodes);
		//Double t = treeHeight - Randomizer.nextExponential(totalRate);
		// Variable declarations.
		Node ranNode = null, ranNode2 = null;
		Sequence nodeLang = null, nodeLang2 = null, newNodeLang;
		String s;
		int idx;
		double[] probs;
		for (int i = 0; i < events.length - 1; i++) {
			System.out.println();
			System.out.println("On branch event: " + i+ " out of " + events.length + ". Next event at " + events[i+1]);
			Double t = events[i] - Randomizer.nextExponential(totalRate);
			while (t > events[i+1]) {
				//System.out.print("\r"+t);
				// Return array of event probabilities and pick one.
				probs = BorrowingProbs(aliveNodes);
				Integer choice = Randomizer.randomChoicePDF(probs);
				// Mutate.
				if (choice == 0) {
					// Pick a random node at time t.
					idx = Randomizer.nextInt(aliveNodes.size());
					ranNode = aliveNodes.get(idx);
					nodeLang = getSequence(ranNode);
					// Pick a random position in language.
					int pos = Randomizer.nextInt(nodeLang.getData().length());
					int currentTrait = Character.getNumericValue(nodeLang.getData().charAt(pos));
					// If death and noEmptyTraitCheck fails.
					if (currentTrait == 1 && (!noEmptyTraitCheck(nodeLang))) {
						s = nodeLang.getData();
					} else {
						s = replaceCharAt(nodeLang.getData(), pos, Integer.toString((1 - currentTrait)));
					}
					newNodeLang = new Sequence("", s);
					newNodeLang.dataInput.setValue(s, newNodeLang);
					setSubTreeLanguages(ranNode, newNodeLang);
					// Borrow.
				} else if (choice == 1) {
					if (aliveNodes.size() > 1) {
						// Pick two distinct languages at random.
						Node[] borrowNodes = getBorrowingNodes(aliveNodes);
						ranNode = borrowNodes[0];
						nodeLang = getSequence(ranNode);

						ranNode2 = borrowNodes[1];
						nodeLang2 = getSequence(ranNode2);

						if (localDist(ranNode, ranNode2)) {
							// Randomly iterate through language and find a 1.
							int ind = getRandomBirthIndex(nodeLang);
							// Give the 1 to the receiving language.
							s = replaceCharAt(nodeLang2.getData(), ind, Integer.toString(1));
							newNodeLang = new Sequence("", s);
							newNodeLang.dataInput.setValue(s, newNodeLang);
							setSubTreeLanguages(ranNode2, newNodeLang);
						}
					}
				}
				t -= Randomizer.nextExponential(totalRate);
			}
			aliveNodes = getAliveNodes(base, t);
			if (aliveNodes.size() == 0) {
				break;
			}
			totalRate = totalRate(aliveNodes);
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
	protected double[] BorrowingProbs(ArrayList<Node> aliveNodes) throws Exception {
		Double totalRate = totalRate(aliveNodes);
		Double borrowSum = 0.0;
		Double mutateSum = 0.0;
		for (Node n : aliveNodes) {
			borrowSum += getBirths(getSequence(n));
			mutateSum += (getSequence(n)).getData().length();
		}
		double[] probs = new double[2];
		probs[0] = rate * mutateSum / totalRate;
		probs[1] = borrowRate * rate * borrowSum / totalRate;
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
	public Double totalRate(ArrayList<Node> aliveNodes) throws Exception {
		Double borrowSum = 0.0;
		Double mutateSum = 0.0;
		for (Node n : aliveNodes) {
			borrowSum += getBirths(getSequence(n));
			mutateSum += (getSequence(n)).getData().length();
		}
		return rate * mutateSum + borrowRate * rate * birthReduction(aliveNodes,borrowSum);
	}
	
	protected static double birthReduction(ArrayList<Node> aliveNodes, double borrowSum) {
		if (aliveNodes.size() > 0) { 
			int seq_length = ((Sequence) aliveNodes.get(0).getMetaData("lang")).getData().length();
			for (int j = 0; j < seq_length; j++) {
				String t = getPositionState(aliveNodes, j);
				int births = (int) t.chars().filter(ch -> ch =='1').count();
				if (births == t.length()) {
					//System.out.println("All 1's, " + births + " not important.");
					borrowSum -= births;
				} else if (births > 0) {
					//System.out.println(births + " 1's, "+ (births -1)+" not important.");
					borrowSum -= (births-1);
				}
			}
		}
		return borrowSum;
	}
	public void setBirthRate(Double r) {
		this.rate = r;
	}

	public Double getBirthRate() {
		return rate;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	/*
	 * Code used to validate the GTR borrowing method.
	 */
	
	public HashMap<String, Integer> mutateOverTreeBorrowingWithEvents(Tree base) throws Exception {
		HashMap<String, Integer> hevents = new HashMap<String, Integer>();
		Double[] events = getEvents(base);
		setSubTreeLanguages(base.getRoot(), (Sequence) base.getRoot().getMetaData("lang"));
		// Get root node.
		ArrayList<Node> aliveNodes = new ArrayList<Node>();
		aliveNodes.addAll(base.getRoot().getChildren());
		// Get first event.
		Double totalRate = totalRate(aliveNodes);
		// Variable declarations.
		Node ranNode = null, ranNode2 = null;
		Sequence nodeLang = null, nodeLang2 = null, newNodeLang;
		String s;
		int idx;
		int pos = 0;
		double[] probs;
		for (int i = 0; i < events.length - 1; i++) {
			Double t = events[i] - Randomizer.nextExponential(totalRate);
			while (t > events[i+1]) {
				// Return array of event probabilities and pick one.
				probs = BorrowingProbs(aliveNodes);
				Integer choice = Randomizer.randomChoicePDF(probs);
				// Mutate.
				if (choice == 0) {
					// Pick a random node at time t.
					idx = Randomizer.nextInt(aliveNodes.size());
					ranNode = aliveNodes.get(idx);
					nodeLang = getSequence(ranNode);
					// Pick a random position in language.
					pos = Randomizer.nextInt(nodeLang.getData().length());
					int currentTrait = Character.getNumericValue(nodeLang.getData().charAt(pos));
					// If death and noEmptyTraitCheck fails.
					if (currentTrait == 1 && (!noEmptyTraitCheck(nodeLang))) {
						s = nodeLang.getData();
					} else {
						s = replaceCharAt(nodeLang.getData(), pos, Integer.toString((1 - currentTrait)));
					}
					newNodeLang = new Sequence("", s);
					newNodeLang.dataInput.setValue(s, newNodeLang);
					setSubTreeLanguages(ranNode, newNodeLang);
					// Borrow.
				} else if (choice == 1) {
					if (aliveNodes.size() > 1) {
						// Pick two distinct languages at random.
						Node[] borrowNodes = getBorrowingNodes(aliveNodes);
						ranNode = borrowNodes[0];
						nodeLang = getSequence(ranNode);

						ranNode2 = borrowNodes[1];
						nodeLang2 = getSequence(ranNode2);

						if (localDist(ranNode, ranNode2)) {
							// Randomly iterate through language and find a 1.
							int ind = getRandomBirthIndex(nodeLang);
							// Give the 1 to the receiving language.
							s = replaceCharAt(nodeLang2.getData(), ind, Integer.toString(1));
							newNodeLang = new Sequence("", s);
							newNodeLang.dataInput.setValue(s, newNodeLang);
							setSubTreeLanguages(ranNode2, newNodeLang);
						}
					}
				}
				t -= Randomizer.nextExponential(totalRate);
				//int count = hevents.getOrDefault(getPositionState(aliveNodes, pos), 0);
				int count = hevents.getOrDefault(ranNode.getNr()+"", 0);
				//hevents.put(getPositionState(aliveNodes, pos), count + 1);
				hevents.put(ranNode.getNr()+"", count + 1);
				//count = hevents.getOrDefault("total", 0);
				//hevents.put("total", count + 1);
			}
			aliveNodes = getAliveNodes(base, t);
			if (aliveNodes.size() == 0) {
				break;
			}
			totalRate = totalRate(aliveNodes);
		}
		return hevents;
	}

	public static String getPositionState(ArrayList<Node> aliveNodes, int pos) {
		String res = "";
		for (Node n : aliveNodes) {
			res += ((Sequence) n.getMetaData("lang")).getData().charAt(pos);
		}
		return res;
	}

	public double twoLangRate(String key) {
		if (Integer.parseInt(key) == 000 || Integer.parseInt(key) == 111) {
			return rate + rate + rate;
		} else if (Integer.parseInt(key) == 100 || Integer.parseInt(key) == 001 || Integer.parseInt(key) == 010) {
			return rate + rate * (borrowRate / 2.0 + 1) + rate * (borrowRate / 2.0 + 1);
		} else {
			return rate + rate + rate * (borrowRate + 1);
		}
	}

	@Override
	public String toString() {
		String s = "";
		s += "GTR";
		return s;
	}
}
