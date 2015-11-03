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
import beast.evolution.alignment.CognateSet;
import beast.evolution.alignment.Language;
import beast.evolution.substitutionmodel.LanguageSubsitutionModel;
import beast.evolution.tree.Tree;
import beast.util.XMLParser;
import beast.util.XMLProducer;

public class LanguageSequenceGen extends beast.core.Runnable {
	public Input<Language> m_rootInput = new Input<Language>("root", "inital language", Validate.REQUIRED);
	public Input<Object> m_subModelInput = new Input<Object>("subModel", "subsitution model for tree", Validate.REQUIRED);
	public Input<Tree> m_treeInput = new Input<Tree>("tree", "phylogenetic beast.tree with sequence data in the leafs", Validate.REQUIRED);
	public Input<Double> m_borrowingRateInput = new Input<Double>("borrowingRate", "rate of borrowing", 0.0);
	public Input<Double> m_borrowingRateZInput = new Input<Double>("borrowingRateZ", "local borrowing distance", 0.0);
	public Input<String> m_outputFileNameInput = new Input<String>(
            "outputFileName",
            "If provided, simulated alignment is written to this file rather "
            + "than to standard out.");
	public Input<List<MergeDataWith>> mergeListInput = new Input<List<MergeDataWith>>("merge", "specifies template used to merge the generated alignment with", new ArrayList<MergeDataWith>());
    public Input<Integer> iterationsInput = new Input<Integer>("iterations","number of times the data is generated", 1);
	
    protected Language root;
    protected LanguageSubsitutionModel m_subModel;
    protected Tree m_tree;
    protected Double m_borrowingRate;
    protected Double m_borrowingRateZ;
    protected String m_outputFileName;
    protected Integer iterations;
    
    @Override
	public void initAndValidate() throws Exception {
		root = m_rootInput.get();
		m_tree = m_treeInput.get();
		m_subModel = (LanguageSubsitutionModel) m_subModelInput.get();
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
	            
	            Language root = ((Input<Language>) plugin.getInput("root")).get();
	            Tree tree = ((Input<Tree>) plugin.getInput("tree")).get();
	            LanguageSubsitutionModel pSubModel = ((Input<LanguageSubsitutionModel>) plugin.getInput("subModel")).get();
	            Double pBorrowingRate = ((Input<Double>) plugin.getInput("borrowingRate")).get();
	            Double pBorrowingRateZ = ((Input<Double>) plugin.getInput("borrowingRateZ")).get();

	            // feed to sequence simulator and generate leaves
	            LanguageSequenceGen treeSimulator = new LanguageSequenceGen();
	            treeSimulator.init(root, tree, pSubModel,pBorrowingRate, pBorrowingRateZ, nReplications);
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
