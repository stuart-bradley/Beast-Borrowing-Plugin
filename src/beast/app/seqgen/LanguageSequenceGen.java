package beast.app.seqgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import beast.core.BEASTInterface;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Sequence;
import beast.evolution.missingdatamodel.MissingDataModel;
import beast.evolution.substitutionmodel.LanguageSubsitutionModel;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.XMLParser;
import beast.util.XMLProducer;

/*
 * LanguageSequenceGen main class 
 * 
 * Designed similarly to the BEAST2 SeqGen class, 
 * but uses LangSeqGen language models.
 * 
 * @author Stuart Bradley (sbra886@aucklanduni.ac.nz)
 * @version 1.0
 * 
 * Honestly surprised
 * I've made it this far, not much
 * left to finish off
 */
public class LanguageSequenceGen extends beast.core.Runnable {
	public Input<Sequence> m_rootInput = new Input<Sequence>("root", "inital language", Validate.REQUIRED);
	public Input<LanguageSubsitutionModel> m_subModelInput = new Input<LanguageSubsitutionModel>("subModel",
			"subsitution model for tree", Validate.REQUIRED);
	public Input<Tree> m_treeInput = new Input<Tree>("tree", "phylogenetic beast.tree with sequence data in the leafs",
			Validate.REQUIRED);
	public Input<MissingDataModel> m_missingModelInput = new Input<MissingDataModel>("missingModel",
			"missing data model for alignment", Validate.REQUIRED);

	protected static Sequence root;
	protected static LanguageSubsitutionModel m_subModel;
	protected static Tree m_tree;
	protected static MissingDataModel m_missingModel;

	@Override
	public void initAndValidate() {
	}

	/*
	 * Simulates the alignment.
	 * 
	 * @param numMeaningClasses, number of meaning classes defined 
	 * by the user.
	 * 
	 * @return Generated alignment and positions of meaning classes.
	 */
	public HashMap<Alignment, String> simulate(Integer numMeaningClasses) throws Exception {
		Alignment cognateSet = new Alignment();
		ArrayList<Sequence> newSeqs = new ArrayList<Sequence>();
		String meaningClasses = "0";

		for (int i = 0; i < numMeaningClasses; i++) {
			Tree newTree;
			m_tree.getRoot().setMetaData("lang", root);
			if (m_subModel.getBorrowRate() == 0.0) {
				newTree = m_subModel.mutateOverTree(m_tree);
			} else {
				newTree = m_subModel.mutateOverTreeBorrowing(m_tree);
			}
			// Base Case.
			if (i == 0) {
				newSeqs = new ArrayList<Sequence>();
				for (Node n : newTree.getExternalNodes()) {
					Sequence d = new Sequence(n.getID(), LanguageSubsitutionModel.getSequence(n).getData());
					newSeqs.add(d);
				}
			} else {
				ArrayList<Sequence> tmp = new ArrayList<Sequence>();
				ArrayList<String> tmp_taxon = new ArrayList<String>();
				for (Node n : newTree.getExternalNodes()) {
					tmp.add(LanguageSubsitutionModel.getSequence(n));
					tmp_taxon.add(n.getID());
				}

				meaningClasses += " " + newSeqs.get(1).getData().length();
				for (int j = 0; j < newSeqs.size(); j++) {
					String newSeq = newSeqs.get(j).getData();
					newSeq += tmp.get(j).getData();
					// Create sequence.
					Sequence d = new Sequence(tmp_taxon.get(j), newSeq);
					newSeqs.set(j, d);
				}
			}
		}
		newSeqs = removeEmptyColumns(newSeqs);
		newSeqs = m_missingModel.generateMissingData(newSeqs, meaningClasses);
		cognateSet = new Alignment();
		cognateSet.dataTypeInput.setValue("binary", cognateSet);
		cognateSet.setID(m_subModel.toString());
		for (Sequence d : newSeqs) {
			cognateSet.sequenceInput.setValue(d, cognateSet);
		}
		HashMap<Alignment, String> result = new HashMap<Alignment, String>();
		result.put(cognateSet, meaningClasses);
		return result;
	}

	/*
	 * Removes 0 columns from language matrix.
	 * 
	 * @param language matrix.
	 * 
	 * @return language matrix.
	 */
	private static ArrayList<Sequence> removeEmptyColumns(ArrayList<Sequence> oldS) throws Exception {
		ArrayList<Sequence> newS = new ArrayList<Sequence>();
		ArrayList<Integer> emptyColumns = new ArrayList<Integer>();
		int seqLength = oldS.get(0).getData().length();

		for (int i = 0; i < seqLength; i++) {
			boolean empty = true;
			for (Sequence s : oldS) {
				if (s.getData().charAt(i) != '0') {
					empty = false;
					break;
				}
			}
			if (empty) {
				emptyColumns.add(i);
			}
		}
		for (Sequence s : oldS) {
			int removalCounter = 0;
			StringBuilder sb = new StringBuilder(s.getData());
			for (int i : emptyColumns) {
				sb.deleteCharAt(i - removalCounter);
				removalCounter += 1;
			}
			Sequence newSeq = new Sequence(s.getTaxon(), sb.toString());
			newS.add(newSeq);
		}

		return newS;
	}

	public static void printUsageAndExit() {
		System.out.println("Usage: java " + SequenceSimulator.class.getName()
				+ " <beast file> <nr of instantiations> [<output file>]");
		System.out.println("Produces an alignment of languages simulated from a tree, and a root language.");
		System.out.println("<beast file> is name of the path beast file containing the treelikelihood.");
		System.out.println(
				"<nr of instantiations> is the number of instantiations to produce an alignment with the No Empty Trait Assumption.");
		System.out.println(
				"<output file> optional name of the file to write the sequence to. By default, the sequence is written to standard output.");
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
			HashMap<Alignment, String> alignmentHash = treeSimulator.simulate(Integer.parseInt(args[1]));
			Alignment alignment = (Alignment) alignmentHash.keySet().toArray()[0];
			String meaningClasses = alignmentHash.get(alignment);
			sXML = producer.toRawXML(alignment);
			String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
			out.println("<beast version='2.0'>");
			out.print(sXML);
			out.println("<!-- Meaning Classes: " + meaningClasses + " -->");
			out.println("<!-- Created at: " + timestamp + " -->");
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