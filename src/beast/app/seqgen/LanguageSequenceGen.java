package beast.app.seqgen;

import java.io.PrintStream;
import java.util.ArrayList;

import beast.core.Input;
import beast.core.Input.Validate;
import beast.evolution.alignment.CognateSet;
import beast.evolution.alignment.Language;
import beast.evolution.substitutionmodel.SubstitutionModel;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;
import beast.util.XMLProducer;

public class LanguageSequenceGen extends beast.core.Runnable {
	public Input<Language> m_rootInput = new Input<Language>("root", "inital language", Validate.REQUIRED);
	public Input<SubstitutionModel.Base> m_subModelInput = new Input<SubstitutionModel.Base>("subModel", "subsitution model for tree", Validate.REQUIRED);
	public Input<Integer> m_treeHeightInput = new Input<Integer>("treeHeight", "number of leaves for the tree", Validate.REQUIRED);
	public Input<Double> m_treeBranchRateInput = new Input<Double>("treeBranchRate", "rate of tree branching", Validate.REQUIRED);
	public Input<Double> m_borrowingRateInput = new Input<Double>("borrowingRate", "rate of borrowing", Validate.OPTIONAL);
	public Input<String> m_outputFileNameInput = new Input<String>(
            "outputFileName",
            "If provided, simulated alignment is written to this file rather "
            + "than to standard out.");
    public Input<Integer> iterationsInput = new Input<Integer>("iterations","number of times the data is generated", 1);
	
    protected Language root;
    protected SubstitutionModel.Base m_subModel;
    protected Integer m_treeHeight;
    protected Double m_treeBranchRate;
    protected Double m_borrowingRate;
    protected String m_outputFileName;
    protected Integer iterations;
    
    @Override
	public void initAndValidate() throws Exception {
		root = m_rootInput.get();
		m_subModel = m_subModelInput.get();
		m_treeHeight = m_treeHeightInput.get();
		m_treeBranchRate = m_treeBranchRateInput.get();
		m_borrowingRate = m_borrowingRateInput.get();
		m_outputFileName = m_outputFileNameInput.get();
		iterations = iterationsInput.get();
	}

	@Override
	public void run() throws Exception {
		for (int i = 0; i < iterationsInput.get(); i++) {
			CognateSet cognateSet = simulate();
			
			// Write output to stdout or file
	        PrintStream pstream;
	        if (m_outputFileName == null)
	            pstream = System.out;
	        else
	            pstream = new PrintStream(m_outputFileName);
	        pstream.println(new XMLProducer().toRawXML(cognateSet));
		}
	}
	
	public CognateSet simulate() throws Exception {
		CognateSet cognateSet = new CognateSet(root);
		cognateSet.setID("LanguageSequenceSimulator");
		Node rootNode = new Node("root");
		rootNode.setMetaData("lang", cognateSet.getLanguage(0));
		rootNode.setHeight(0);
		
		Tree tree = new Tree(rootNode);
		tree = randomTree(tree, m_treeHeight, m_treeBranchRate);
		
		return cognateSet;
	}
	
	public Tree randomTree(Tree rootTree, Integer numLeaves, Double branchRate) {
		ArrayList<Node> currLeaves = new ArrayList<Node>(); 
		ArrayList<Node> newLeaves = new ArrayList<Node>();
		currLeaves.add(rootTree.getRoot());
		Language rootLang = (Language) rootTree.getRoot().getMetaData("lang");
		Node childLeft, childRight;
		
		while (currLeaves.size() < numLeaves) {
			for (Node parent : currLeaves) {
				childLeft = new Node();
				childRight = new Node();
				
				// Left child.
				double t = Randomizer.nextExponential(branchRate);
				childLeft.setParent(parent);
				parent.addChild(childLeft);
				childLeft.setHeight(parent.getHeight()+t);
				childLeft.setMetaData("lang", rootLang);
				newLeaves.add(childLeft);
				rootTree.addNode(childLeft);
				// Right child.
				t = Randomizer.nextExponential(branchRate);
				childRight.setParent(parent);
				parent.addChild(childRight);
				childRight.setHeight(parent.getHeight()+t);
				childRight.setMetaData("lang", rootLang);
				newLeaves.add(childRight);
				rootTree.addNode(childRight);
			}
			currLeaves = new ArrayList<Node>(newLeaves);
			newLeaves = new ArrayList<Node>();
		}
		return rootTree;
	}

}
