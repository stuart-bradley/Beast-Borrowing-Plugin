package beast.evolution.substitutionmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.alignment.Sequence;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
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
public class ExplicitBinaryStochasticDollo extends LanguageSubsitutionModel {
	/** Backward and forward substitution rates. */
	public Input<Double> rate01Input = new Input<Double>("birth", "substitution rate for 0 to 1 (birth)");
	public Input<Double> rate10Input = new Input<Double>("death", "substitution rate for 1 to 0 (death)");

	/** Birth and Death rates */
	private double b;
	private double d;

	public ExplicitBinaryStochasticDollo() throws Exception {
	}

	public ExplicitBinaryStochasticDollo(double birth, double death, double borrow, double z, boolean e) {
		this.setB(birth);
		this.setD(death);
		this.setBorrowRate(borrow);
		this.setBorrowZ(z);
		this.setNoEmptyTrait(e);
	}

	/*
	 * Checks birth and death rates are acceptable.
	 * 
	 * @see
	 * beast.evolution.substitutionmodel.SubstitutionModel.Base#initAndValidate(
	 * )
	 */
	@Override
	public void initAndValidate() {
		this.b = rate01Input.get();
		this.d = rate10Input.get();
		this.borrowRate = borrowInput.get();
		this.borrowZ = borrowZInput.get();
		this.noEmptyTrait = noEmptyTraitInput.get();
	}

	/*
	 * Mutates a language according to the Stochastic Dollo model.
	 * 
	 * @param l Language to mutate.
	 * 
	 * @param T Mutation time.
	 * 
	 * @return newLang Mutated language.
	 */
	public Sequence mutateLang(Sequence l, double T) throws Exception {
		Sequence newLang = new Sequence("", l.getData());
		double[] probs = new double[2];
		// Mutation proper.
		double t = Randomizer.nextExponential(getB() + getD() * getBirths(newLang));
		while (t < T) {
			// Set probabilities for language.
			probs[0] = getD() * getBirths(newLang) / (getB() + getD() * getBirths(newLang));
			probs[1] = getB() / (getB() + getD() * getBirths(newLang));
			// If death.
			if (Randomizer.randomChoice(probs) == 0) {
				// Find random alive trait, and kill it.
				for (int randomNum : Randomizer.shuffled(newLang.getData().length())) {
					if (Character.getNumericValue(newLang.getData().charAt(randomNum)) != 0) {
						if (noEmptyTraitCheck(newLang)) {
							String newSeq = replaceCharAt(newLang.getData(), randomNum, Integer.toString(0));
							newLang.dataInput.setValue(newSeq, newLang);
						}
						break;
					}
				}
				// Else birth.
			} else {
				String newSeq = newLang.getData() + '1';
				newLang.dataInput.setValue(newSeq, newLang);
			}
			t += Randomizer.nextExponential(getB() + getD() * getBirths(newLang));
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
					addEmptyTrait(base, child);
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
		String[] stringAliveNodes = {};
		Double totalRate = null;
		Node ranNode = null, ranNode2 = null;
		Sequence nodeLang = null, nodeLang2 = null, newNodeLang = null;
		String s;
		int idx;
		double[] probs = new double[3];
		for (int i = 0; i < events.length - 1; i++) {
			if (events[i] == 0.0) {
				break;
			}
			aliveNodes = getAliveNodes(base, events[i+1]);
			totalRate = totalRate(stringAliveNodes);
			Double t = events[i] - Randomizer.nextExponential(totalRate);
			//System.out.println();
			//System.out.println("On branch event: " + i+ " out of " + (events.length/2-1) + ". Next event at " + events[i+1]);
			while (t > events[i+1]) {
				//System.out.print("\r"+t);
				probs = BorrowingProbs(stringAliveNodes, totalRate);
				Integer choice = Randomizer.randomChoicePDF(probs);
				if (choice == 0) {
					idx = Randomizer.nextInt(aliveNodes.size());
					ranNode = aliveNodes.get(idx);
					nodeLang = getSequence(ranNode);
					s = nodeLang.getData() + '1';
					newNodeLang = new Sequence("", s);
					setSubTreeLanguages(ranNode, newNodeLang);
					addEmptyTrait(base, ranNode);
					// Death.
				} else if (choice == 1) {
					idx = Randomizer.nextInt(aliveNodes.size());
					ranNode = aliveNodes.get(idx);
					nodeLang = getSequence(ranNode);
					// Find random alive trait, and kill it.
					for (Integer j : Randomizer.shuffled(nodeLang.getData().length())) {
						if (Character.getNumericValue(nodeLang.getData().charAt(j)) != 0) {
							if (noEmptyTraitCheck(nodeLang)) {
								s = replaceCharAt(nodeLang.getData(), j, Integer.toString(0));
								newNodeLang = new Sequence("", s);
								newNodeLang.dataInput.setValue(s, newNodeLang);
								setSubTreeLanguages(ranNode, newNodeLang);
							}
							break;
						}
					}
					ranNode.setMetaData("lang", nodeLang);
					// Borrowing.
				} else if (choice == 2) {
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
	protected double[] BorrowingProbs(String[] aliveNodes, Double totalRate) throws Exception {
		double[] probs = new double[3];
		Double death = 0.0, bo = 0.0;
		for (String n : aliveNodes) {
			int births = (int) n.chars().filter(ch -> ch =='1').count();
			// mu*k1 + ... + mu*kn
			death += getD() * births;
			// b*(k1 + ... + kn)
			bo += births;
		}
		probs[0] = (aliveNodes.length * getB()) / totalRate; // Birth
		probs[1] = (death) / totalRate; // Death
		probs[2] = (getD() * borrowRate * bo) / totalRate; // Borrow
		return probs;
	}

	/*
	 * Total rate of mutation.
	 * 
	 * @param aliveNodes, see aliveNodes(base, t).
	 * 
	 * @param borrow borrowing rate.
	 * 
	 * @return Double, total rate.
	 */
	protected Double totalRate(String[] aliveNodes) throws Exception {
		Double birthRate = aliveNodes.length * getB();
		Double deathSum = 0.0;
		Double borrowSum = 0.0;
		for (String n : aliveNodes) {
			int births = (int) n.chars().filter(ch -> ch =='1').count();
			deathSum += getD() * births;
			borrowSum += births;
		}
		return birthRate + deathSum + (getBorrowRate()*borrowSum);
	}

	/*
	 * Adds empty traits to other languages when a birth occurs.
	 * 
	 * @param t Tree.
	 * 
	 * @param newNodeLang, Language with mutated languages.
	 * 
	 */
	protected void addEmptyTrait(Tree t, Node newLangNode) throws Exception {
		// Get all nodes in two lists.
		List<Node> children = newLangNode.getAllChildNodes();
		// Find nodes that aren't children (or trait lang).
		List<Node> allNodes = t.getInternalNodes();
		allNodes.addAll(t.getExternalNodes());
		allNodes.removeAll(children);
		allNodes.remove(newLangNode);

		// Calculate number of [new] mutations in new language.
		Sequence newLang = getSequence(newLangNode);
		for (Node n : allNodes) {
			Sequence nLang = getSequence(n);
			String s = nLang.getData();
			Sequence newNodeLang = new Sequence("", s);
			while (newNodeLang.getData().length() < newLang.getData().length()) {
				String sNew = newNodeLang.getData() + '0';
				// System.out.println(newNodeLang);
				newNodeLang.dataInput.setValue(sNew, this);
			}
			n.setMetaData("lang", newNodeLang);
		}
	}

	public void setBirthRate(Double r) {
		this.b = r;
	}

	public Double getBirthRate() {
		return b;
	}

	/*
	 * Auto-generated getters/setters.
	 */

	public double getB() {
		return b;
	}

	public void setB(double b) {
		this.b = b;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	@Override
	public String toString() {
		String s = "";
		s += "SD";
		return s;
	}
	
	private static ArrayList<Node> getLeafNodes (Node child) {
		ArrayList<Node> leaves = new ArrayList<Node>();
		if (child.getChildren().isEmpty()) {
	        leaves.add(child);
	    } else {
	        for (Node c : child.getChildren()) {
	            leaves.addAll(getLeafNodes(c));
	        }
	    }
		return leaves;
	}
}
