package beast.app.seqgen;

import java.io.PrintStream;

import beast.core.Input;
import beast.core.Input.Validate;
import beast.evolution.alignment.CognateSet;
import beast.evolution.alignment.Language;
import beast.evolution.branchratemodel.BranchRateModel;
import beast.evolution.substitutionmodel.SubstitutionModel;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.XMLProducer;

public class LanguageSequenceGen extends beast.core.Runnable {
	public Input<Language> m_rootInput = new Input<Language>("root", "inital language", Validate.REQUIRED);
	public Input<SubstitutionModel.Base> m_subModelInput = new Input<SubstitutionModel.Base>("subModel", "subsitution model for tree", Validate.REQUIRED);
	public Input<Double> m_treeHeightInput = new Input<Double>("treeHeight", "height of resulting tree", Validate.REQUIRED);
	public Input<BranchRateModel.Base> m_pBranchRateModelInput = new Input<BranchRateModel.Base>("branchRateModel", "A model describing the rates on the branches of the beast.tree.");
	public Input<String> m_outputFileNameInput = new Input<String>(
            "outputFileName",
            "If provided, simulated alignment is written to this file rather "
            + "than to standard out.");
    public Input<Integer> iterationsInput = new Input<Integer>("iterations","number of times the data is generated", 1);
	
    protected Language root;
    protected SubstitutionModel.Base m_subModel;
    protected Double m_treeHeight;
    protected BranchRateModel.Base m_pBranchRateModel;
    protected String m_outputFileName;
    protected Integer iterations;
    
    @Override
	public void initAndValidate() throws Exception {
		root = m_rootInput.get();
		m_subModel = m_subModelInput.get();
		m_treeHeight = m_treeHeightInput.get();
		m_pBranchRateModel = m_pBranchRateModelInput.get();
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
		rootNode.setMetaData("language", cognateSet.getLanguage(0));
		rootNode.setHeight(0);
		Tree tree = new Tree(rootNode);
		double branchRate = m_pBranchRateModel.getRateForBranch(rootNode);
		return cognateSet;
	}

}
