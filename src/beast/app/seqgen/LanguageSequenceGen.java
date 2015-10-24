package beast.app.seqgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import beast.core.BEASTInterface;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.CognateSet;
import beast.evolution.alignment.Language;
import beast.evolution.branchratemodel.BranchRateModel;
import beast.evolution.likelihood.TreeLikelihood;
import beast.evolution.sitemodel.SiteModel;
import beast.evolution.substitutionmodel.SubstitutionModel;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;
import beast.util.XMLParser;
import beast.util.XMLProducer;

public class LanguageSequenceGen extends beast.core.Runnable {
	public Input<Language> m_rootInput = new Input<Language>("root", "inital language", Validate.REQUIRED);
	public Input<SubstitutionModel.Base> m_subModelInput = new Input<SubstitutionModel.Base>("subModel", "subsitution model for tree", Validate.REQUIRED);
	public Input<Tree> m_treeInput = new Input<Tree>("tree", "phylogenetic beast.tree with sequence data in the leafs", Validate.REQUIRED);
	public Input<Double> m_borrowingRateInput = new Input<Double>("borrowingRate", "rate of borrowing", 0.0);
	public Input<Double> m_borrowingRateZInput = new Input<Double>("borrowingZRate", "local borrowing distance", 0.0);
	public Input<String> m_outputFileNameInput = new Input<String>(
            "outputFileName",
            "If provided, simulated alignment is written to this file rather "
            + "than to standard out.");
	public Input<List<MergeDataWith>> mergeListInput = new Input<List<MergeDataWith>>("merge", "specifies template used to merge the generated alignment with", new ArrayList<MergeDataWith>());
    public Input<Integer> iterationsInput = new Input<Integer>("iterations","number of times the data is generated", 1);
	
    protected Language root;
    protected SubstitutionModel.Base m_subModel;
    protected Tree m_tree;
    protected Double m_borrowingRate;
    protected Double m_borrowingRateZ;
    protected String m_outputFileName;
    protected Integer iterations;
    
    @Override
	public void initAndValidate() throws Exception {
		root = m_rootInput.get();
		m_tree = m_treeInput.get();
		m_subModel = m_subModelInput.get();
		m_borrowingRate = m_borrowingRateInput.get();
		m_borrowingRateZ = m_borrowingRateZInput.get();
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
	        for (MergeDataWith merge : mergeListInput.get()) {
	        	merge.process(cognateSet, i);
	        } 
		}
	}
	
	public CognateSet simulate() throws Exception {
		CognateSet cognateSet = new CognateSet(root);
		cognateSet.setID("LanguageSequenceSimulator");

		
		m_tree.getRoot().setMetaData("lang", cognateSet.getLanguage(0));
        cognateSet.setID("LanguageSequenceSimulator");
		
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
				//t = Randomizer.nextExponential(branchRate);
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
	
	/**
     * find a treelikelihood object among the plug-ins by recursively inspecting plug-ins *
     */
    static TreeLikelihood getTreeLikelihood(BEASTInterface plugin) throws Exception {
        for (BEASTInterface plugin2 : plugin.listActivePlugins()) {
            if (plugin2 instanceof TreeLikelihood) {
                return (TreeLikelihood) plugin2;
            } else {
                TreeLikelihood likelihood = getTreeLikelihood(plugin2);
                if (likelihood != null) {
                    return likelihood;
                }
            }
        }
        return null;
    }
	
	public static void printUsageAndExit() {
        System.out.println("Usage: java " + SequenceSimulator.class.getName() + " <beast file> <nr of instantiations> [<output file>]");
        System.out.println("simulates from a treelikelihood specified in the beast file.");
        System.out.println("<beast file> is name of the path beast file containing the treelikelihood.");
        System.out.println("<nr of instantiations> is the number of instantiations to be replicated.");
        System.out.println("<output file> optional name of the file to write the sequence to. By default, the sequence is written to standard output.");
        System.exit(0);
    } // printUsageAndExit

	 @SuppressWarnings("unchecked")
	    public static void main(String[] args) {
	        try {
	            // parse arguments
	            if (args.length < 2) {
	                printUsageAndExit();
	            }
	            String sFile = args[0];
	            int nReplications = Integer.parseInt(args[1]);
	            PrintStream out = System.out;
	            if (args.length == 3) {
	                File file = new File(args[2]);
	                out = new PrintStream(file);
	            }

	            // grab the file
	            String sXML = "";
	            BufferedReader fin = new BufferedReader(new FileReader(sFile));
	            while (fin.ready()) {
	                sXML += fin.readLine();
	            }
	            fin.close();

	            // parse the xml
	            XMLParser parser = new XMLParser();
	            BEASTInterface plugin = parser.parseFragment(sXML, true);

	            // find relevant objects from the model
	            TreeLikelihood treeLikelihood = getTreeLikelihood(plugin);
	            if (treeLikelihood == null) {
	                throw new Exception("No treelikelihood found in file. Giving up now.");
	            }
	            Language root = ((Input<Language>) treeLikelihood.getInput("root")).get();
	            Tree tree = ((Input<Tree>) treeLikelihood.getInput("tree")).get();
	            SubstitutionModel pSubModel = ((Input<SubstitutionModel>) treeLikelihood.getInput("subsitutionModel")).get();
	            Double pBorrowingRate = ((Input<Double>) treeLikelihood.getInput("borrowingRate")).get();
	            Double pBorrowingRateZ = ((Input<Double>) treeLikelihood.getInput("borrowingRateZ")).get();

	            // feed to sequence simulator and generate leaves
	            LanguageSequenceGen treeSimulator = new LanguageSequenceGen();
	            treeSimulator.init(root, tree, pSubModel, pSubModel,pBorrowingRate, pBorrowingRateZ, nReplications);
	            XMLProducer producer = new XMLProducer();
	            CognateSet alignment = treeSimulator.simulate();
	            sXML = producer.toRawXML(alignment);
	            out.println("<beast version='2.0'>");
	            out.println(sXML);
	            out.println("</beast>");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
}
