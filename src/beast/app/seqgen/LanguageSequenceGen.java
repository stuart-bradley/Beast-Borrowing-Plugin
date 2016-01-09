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
import beast.evolution.alignment.Sequence;
import beast.evolution.missingdatamodel.MissingDataModel;
import beast.evolution.missingdatamodel.MissingLanguageModel;
import beast.evolution.substitutionmodel.LanguageSubsitutionModel;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.XMLParser;
import beast.util.XMLProducer;

public class LanguageSequenceGen extends beast.core.Runnable {
	public Input<Sequence> m_rootInput = new Input<Sequence>("root", "inital language", Validate.REQUIRED);
	public Input<LanguageSubsitutionModel> m_subModelInput = new Input<LanguageSubsitutionModel>("subModel", "subsitution model for tree", Validate.REQUIRED);
	public Input<Tree> m_treeInput = new Input<Tree>("tree", "phylogenetic beast.tree with sequence data in the leafs", Validate.REQUIRED);
	public Input<MissingDataModel> m_missingModelInput = new Input<MissingDataModel>("missingModel", "missing data model for alignment", Validate.REQUIRED);
	
    protected static Sequence root;
    protected static LanguageSubsitutionModel m_subModel;
    protected static Tree m_tree;
    protected static MissingDataModel m_missingModel;
    
    @Override
	public void initAndValidate() throws Exception {
	}
	
	public Alignment simulate(Integer numMeaningClasses) throws Exception {
		Alignment cognateSet = new Alignment();
		ArrayList<Sequence> newSeqs = new ArrayList<Sequence>();
		String meaningClasses = "0 ";

		m_tree.getRoot().setMetaData("lang", root);
		cognateSet.sequenceInput.setValue(root, cognateSet);
		Tree newTree;
		for (int i = 0; i < numMeaningClasses; i++) {
			if (m_subModel.getBorrowRate() == 0.0) { 
				newTree = m_subModel.mutateOverTree(m_tree);
			} else {
				newTree = m_subModel.mutateOverTreeBorrowing(m_tree);
			}
			// Base Case.
			if (i == 0) {
				ArrayList<Sequence> tmp = new ArrayList<Sequence>();
				for (Node n : newTree.getExternalNodes()) {
					tmp.add(LanguageSubsitutionModel.getSequence(n));
				}
				for (Sequence d : tmp) {
					cognateSet.sequenceInput.setValue(d, cognateSet);
				}
			} else {
				ArrayList<Sequence> tmp = new ArrayList<Sequence>();
				for (Node n : newTree.getExternalNodes()) {
					tmp.add(LanguageSubsitutionModel.getSequence(n));
				}
				List<Sequence> counts = cognateSet.sequenceInput.get();
				newSeqs = new ArrayList<Sequence>();
				// Grab next meaning class.
				meaningClasses += counts.get(1).getData().length() + " ";
				for (int j = 0; j < tmp.size(); j++) {
					// j+1 to account for ROOT.
					String newSeq = counts.get(j+1).getData();
					newSeq += tmp.get(j).getData();
					//System.out.println(newSeq);
					// Create sequence.
					Sequence d = new Sequence("", newSeq);
					newSeqs.add(d);
				}
				// Recreate and repopulate alignment.
				cognateSet = new Alignment();
				cognateSet.sequenceInput.setValue(root, cognateSet);
				for (Sequence d : newSeqs) {
					cognateSet.sequenceInput.setValue(d, cognateSet);
				}
			}
		}
		Sequence comment = new Sequence("Meaning Class Positions", meaningClasses);
		newSeqs.add(comment);
		newSeqs = m_missingModel.generateMissingData(newSeqs);
		cognateSet = new Alignment();
		cognateSet.sequenceInput.setValue(root, cognateSet);
		for (Sequence d : newSeqs) {
			cognateSet.sequenceInput.setValue(d, cognateSet);
		}
		return cognateSet;
	}
	
	
	
	public static void printUsageAndExit() {
        System.out.println("Usage: java " + SequenceSimulator.class.getName() + " <beast file> <nr of instantiations> [<output file>]");
        System.out.println("Produces an alignment of languages simulated from a tree, and a root language.");
        System.out.println("<beast file> is name of the path beast file containing the treelikelihood.");
        System.out.println("<nr of instantiations> is the number of instantiations to produce an alignment with the No Empty Trait Assumption.");
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
	            
	            root = ((Input<Sequence>) plugin.getInput("root")).get();
	            root.initAndValidate();
	            m_tree = ((Input<Tree>) plugin.getInput("tree")).get();
	            m_tree.initAndValidate();
	            m_subModel = ((Input<LanguageSubsitutionModel>) plugin.getInput("subModel")).get();
	            m_subModel.initAndValidate();
	            m_missingModel = ((Input<MissingDataModel>) plugin.getInput("missingModel")).get();
	            m_subModel.initAndValidate();
	            

	            // feed to sequence simulator and generate leaves
	            LanguageSequenceGen treeSimulator = new LanguageSequenceGen();
	            XMLProducer producer = new XMLProducer();
	            Alignment alignment = treeSimulator.simulate(Integer.parseInt(args[1]));
	            sXML = producer.toRawXML(alignment);
	            out.println("<beast version='2.0'>");
	            out.println(sXML);
	            out.println("</beast>");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	@Override
	public void run() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
