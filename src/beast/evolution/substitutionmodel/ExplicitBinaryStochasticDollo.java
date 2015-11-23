package beast.evolution.substitutionmodel;

import java.util.ArrayList;
import java.util.List;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.alignment.Language;
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
	public Input<Double> rate01Input = new Input<Double>("birth",
			"substitution rate for 0 to 1 (birth), default = 0.5");
	public Input<Double> rate10Input = new Input<Double>("death",
			"substitution rate for 1 to 0 (death), default = 0.5");

	/** Birth and Death rates */
	private double b;
	private double d;
	
	public ExplicitBinaryStochasticDollo() throws Exception {
	}

	public ExplicitBinaryStochasticDollo(double birth, double death) {
		this.setB(birth);
		this.setD(death);
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
	public Language mutateLang(Language l, double T) {
		ArrayList<Integer> s = new ArrayList<Integer>(l.getLanguage());
		Language newLang = new Language(s);
		double[] probs = new double[2];
		// Mutation proper.
		double t = Randomizer.nextExponential(getB() + getD() * newLang.getBirths());
		while (t < T) {
			// Set probabilities for language.
			probs[0] = getD() * newLang.getBirths() / (getB() + getD() * newLang.getBirths());
			probs[1] = getB() / (getB() + getD() * newLang.getBirths());
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
			}
			t += Randomizer.nextExponential(getB() + getD() * newLang.getBirths());
			// Record mutation.
			l.addMutation(t, newLang.getLanguage());
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
	public Tree mutateOverTree(Tree base) {
		ArrayList<Node> currParents = new ArrayList<Node>();
		ArrayList<Node> newParents = new ArrayList<Node>();
		currParents.add(base.getRoot());
		while (currParents.size() > 0) {
			for (Node parent : currParents) {
				List<Node> children = parent.getChildren();
				for (Node child : children) {
					double T = child.getHeight() - parent.getHeight();
					Language parentLang = (Language) parent.getMetaData("lang");
					Language newLang = mutateLang(parentLang, T);
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
	public Tree mutateOverTreeBorrowing(Tree base, Double borrow, Double z) {
		Double treeHeight = getTreeHeight(base);
		ArrayList<Node> aliveNodes = getAliveNodes(base, 0.0);
		Double totalRate = totalRate(aliveNodes, borrow);
		Double t = Randomizer.nextExponential(totalRate);
		Node ranNode = null, ranNode2 = null;
		Language nodeLang = null, nodeLang2 = null, newNodeLang;
		ArrayList<Integer> s;
		int idx;
		double[] probs = new double[3];
		while (t < treeHeight) {
			probs = BorrowingProbs(aliveNodes, borrow);
			Integer choice = Randomizer.randomChoicePDF(probs);
			switch (choice) {
			// Birth.
			case 0:
				idx = Randomizer.nextInt(aliveNodes.size());
				ranNode = aliveNodes.get(idx);
				nodeLang = (Language) ranNode.getMetaData("lang");
				s = new ArrayList<Integer>(nodeLang.getLanguage());
				s.add(1);
				newNodeLang = new Language(s);
				setSubTreeLanguages(ranNode, newNodeLang);
				addEmptyTrait(base, ranNode);
				break;
			// Death.
			case 1:
				idx = Randomizer.nextInt(aliveNodes.size());
				ranNode = aliveNodes.get(idx);
				nodeLang = (Language) ranNode.getMetaData("lang");
				// Find random alive trait, and kill it.
				for (Integer i : getRandLangIndex(nodeLang)) {
					if (nodeLang.getLanguage().get(i) != 0) {
						s = new ArrayList<Integer>(nodeLang.getLanguage());
						newNodeLang = new Language(s);
						newNodeLang.getLanguage().set(i, 0);
						setSubTreeLanguages(ranNode, newNodeLang);
						break;
					}
				}
				ranNode.setMetaData("lang", nodeLang);
				break;
			// Borrowing.
			case 2:
				if (aliveNodes.size() < 2) {
					break;
				}
				while (true) {
					idx = Randomizer.nextInt(aliveNodes.size());
					ranNode = aliveNodes.get(idx);
					nodeLang = (Language) ranNode.getMetaData("lang");
					idx = Randomizer.nextInt(aliveNodes.size());
					ranNode2 = aliveNodes.get(idx);
					nodeLang2 = (Language) ranNode2.getMetaData("lang");
					if (ranNode != ranNode2) {
						break;
					}
				}
				if (localDist(ranNode, ranNode2, z) == false) {
					break;
				} else {
					for (Integer i : getRandLangIndex(nodeLang)) {
						try {
							if (nodeLang.getLanguage().get(i) == 1) {
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
	protected double[] BorrowingProbs(ArrayList<Node> aliveNodes, Double borrow) {
		double[] probs = new double[3];
		Double death = 0.0, bo = 0.0;
		for (Node n : aliveNodes) {
			// mu*k1 + ... + mu*kn
			death += getD() * ((Language) n.getMetaData("lang")).getBirths();
			// b*(k1 + ... + kn)
			bo += ((Language) n.getMetaData("lang")).getBirths();
		}
		Double tR = totalRate(aliveNodes, borrow);
		probs[0] = (aliveNodes.size() * getB()) / tR; // Birth
		probs[1] = (death) / tR; // Death
		probs[2] = (getD() * borrow * bo) / tR; // Borrow
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
	protected Double totalRate(ArrayList<Node> aliveNodes, Double borrow) {
		Double totalRate = aliveNodes.size() * getB();
		Double birthSum = 0.0;
		for (Node n : aliveNodes) {
			totalRate += getD() * ((Language) n.getMetaData("lang")).getBirths();
			birthSum += ((Language) n.getMetaData("lang")).getBirths();
		}
		totalRate += getD() * borrow * birthSum;
		return totalRate;
	}
	
	/*
	 * Adds empty traits to other languages when a birth occurs.
	 * 
	 * @param t Tree.
	 * 
	 * @param newNodeLang, Language with mutated languages.  
	 * 
	 */
	protected void addEmptyTrait(Tree t, Node newLangNode) {
		// Get all nodes in two lists.
		List<Node> children = newLangNode.getAllChildNodes();
		// Find nodes that aren't children (or trait lang).
		List<Node> allNodes = t.getInternalNodes();
		allNodes.addAll(t.getExternalNodes());
		allNodes.removeAll(children);
		allNodes.remove(newLangNode);
		
		// Calculate number of [new] mutations in new language.
		Language newLang = (Language) newLangNode.getMetaData("lang");
		for (Node n : allNodes) {
				Language nLang = (Language) n.getMetaData("lang");
				ArrayList<Integer> s = nLang.getLanguage();
				Language newNodeLang = new Language(s);
				while (newNodeLang.getLanguage().size() < newLang.getLanguage().size()) {
					newNodeLang.getLanguage().add(0);
				}
				n.setMetaData("lang", newNodeLang);
		}
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
}
