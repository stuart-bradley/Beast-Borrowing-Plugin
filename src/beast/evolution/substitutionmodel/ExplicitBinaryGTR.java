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
		String[] stringAliveNodes = {};
		int[] traits = {};
		int numberOfLangs = 0;
		// Get first event.
		Double totalRate = null;
		//Double t = treeHeight - Randomizer.nextExponential(totalRate);
		// Variable declarations.
		int idx;
		double[] probs;
		for (int i = 0; i < events.length - 1; i++) {
			if (events[i] == 0.0) {
				break;
			}
			aliveNodes = getAliveNodes(base, events[i+1]);
			stringAliveNodes = getSequences(aliveNodes);
			numberOfLangs = stringAliveNodes.length;
			traits = getBirths(stringAliveNodes, numberOfLangs);
			totalRate = totalRate(stringAliveNodes, traits, numberOfLangs);
			Double t = events[i] - Randomizer.nextExponential(totalRate);
			System.out.println();
			System.out.println("On branch event: " + (i+1)+ " out of " + (events.length/2) + ". Next event at " + events[i+1]);
			while (t > events[i+1]) {
				System.out.print("\r"+t);
				// Return array of event probabilities and pick one.
				probs = BorrowingProbs(stringAliveNodes, totalRate, traits, numberOfLangs);
				Integer choice = Randomizer.randomChoicePDF(probs);
				// Mutate.
				if (choice == 0) {
					// Pick a random node at time t.
					idx = Randomizer.nextInt(stringAliveNodes.length);
					// Pick a random position in language.
					int pos = Randomizer.nextInt(stringAliveNodes[idx].length());
					int currentTrait = Character.getNumericValue(stringAliveNodes[idx].charAt(pos));
					// If death and noEmptyTraitCheck fails.
					if (currentTrait == 1 && (!noEmptyTraitCheck(stringAliveNodes[idx]))) {
						stringAliveNodes[idx] = stringAliveNodes[idx];
					} else {
						stringAliveNodes[idx] = replaceCharAt(stringAliveNodes[idx], pos, Integer.toString((1 - currentTrait)));
					} 
					// Change trait structure
					if (stringAliveNodes[idx].charAt(pos) == '1') {
						traits[idx]++;
					} else {
						traits[idx]--;
					}
					
					// Borrow.
				} else if (choice == 1) {
					if (aliveNodes.size() > 1) {
						// Pick two distinct languages at random.
						int[] bN = getBorrowingNodes(stringAliveNodes, traits, numberOfLangs);

						if (localDist(aliveNodes.get(bN[0]), aliveNodes.get(bN[1]))) {
							// Randomly iterate through language and find a 1.
							int ind = getRandomBirthIndex(stringAliveNodes[bN[0]], traits[bN[0]]);
							// If recieving language is going 0 -> 1.
							if (ind > -1 && stringAliveNodes[bN[1]].charAt(ind) == '0') {
								traits[bN[1]]++;
							}
							// Give the 1 to the receiving language.
							stringAliveNodes[bN[1]] = replaceCharAt(stringAliveNodes[bN[1]], ind, Integer.toString(1));
						}
					}
				}
				totalRate = totalRate(stringAliveNodes, traits, numberOfLangs);
				t -= Randomizer.nextExponential(totalRate);
			}
			setLangs(aliveNodes, stringAliveNodes);
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
	protected double[] BorrowingProbs(String[] aliveNodes, Double totalRate, int[] traits, int numberOfLangs) throws Exception {
		Double borrowSum = 0.0;
		int seq_length = aliveNodes[0].length();
		for (int n : traits) {
			borrowSum += n;
		}
		double[] probs = new double[2];
		probs[0] = rate * (numberOfLangs * seq_length) / totalRate;
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
	public Double totalRate(String[] aliveNodes, int[] traits, int numberOfLangs) throws Exception {
		Double borrowSum = 0.0;
		int seq_length = aliveNodes[0].length();
		for (int n : traits) {
			borrowSum += n;
		}
		return rate * (numberOfLangs * seq_length) + borrowRate * rate * borrowSum;
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

	@Override
	public String toString() {
		String s = "";
		s += "GTR";
		return s;
	}
}
