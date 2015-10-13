package beast.evolution.substitutionmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import beast.evolution.alignment.CognateSet;
import beast.evolution.alignment.Language;
import beast.evolution.datatype.DataType;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

public abstract class LanguageSubsitutionModel extends SubstitutionModel.Base {
	
	public abstract void initAndValidate();
	public abstract Language mutateLang(Language l, CognateSet c, double T);
	public abstract Tree mutateOverTree(Tree base, CognateSet c);
	public abstract Tree mutateOverTreeBorrowing(Tree base, CognateSet c, Double borrow, Double z);
	protected abstract double[] BorrowingProbs(ArrayList<Node> aliveNodes, Double borrow);
	protected abstract Double totalRate (ArrayList<Node> aliveNodes, Double borrow);
	
	protected ArrayList<Integer> getRandLangIndex(Language l) {
		ArrayList<Integer> randInts = new ArrayList<Integer>();
		for (int i = 0; i < l.getLanguage().size(); i++) {
			randInts.add(i);
		}
		
		Collections.shuffle(randInts);
		
		return randInts;
		
	}
	
	protected boolean localDist(Node L1, Node L2, Double z) {
		if (z == 0) {
			return true;
		}
		Node parent1, parent2;
		Double dist1 = 0.0, dist2 = 0.0;
		while (dist1 <= z && dist2 <= z) {
			parent1 = L1.getParent();
			parent2 = L1.getParent();
			
			if (parent1 == parent2) {
				return true;
			}
			
			dist1 = L1.getHeight() - parent1.getHeight();
			dist2 = L2.getHeight() - parent2.getHeight();
			
			L1 = parent1;
			L2 = parent2;
		}
		return false;
	}
	
	protected Double getTreeHeight(Tree base) {
		Node[] nodes = base.getNodesAsArray();
	    Arrays.sort(nodes, new Comparator<Node>() {
	        @Override
	        public int compare(Node o1, Node o2) {
	            return new Double (o1.getHeight()).compareTo(o2.getHeight());
	        }
	    });	
	    
	    return nodes[nodes.length-1].getHeight();
	}
	
	protected void setSubTreeLanguages(Node subRoot, Language newLang) {
		subRoot.setMetaData("lang", newLang);
		for (Node n : subRoot.getAllChildNodes()) {
			n.setMetaData("lang", newLang);
		}
	}
	
	protected ArrayList<Node> getAliveNodes(Tree base, Double t) {
		ArrayList<Node> aliveNodes = new ArrayList<Node>();
		
		Node root = base.getRoot();
		for (Node child : root.getChildren()) {
			if (child.getHeight() >= t) {
				aliveNodes.add(child);
			} else {
				aliveNodes.addAll(aliveNodes(child, t));
			}
		}

		return aliveNodes;
	}
	
	protected ArrayList<Node> aliveNodes(Node curr, Double t) {
		ArrayList<Node> aN = new ArrayList<Node>();
		for (Node child : curr.getChildren()) {
			if (child.getHeight() >= t) {
				aN.add(child);
			} else {
				aN.addAll(aliveNodes(child, t));
			}
		}
		return aN;
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
