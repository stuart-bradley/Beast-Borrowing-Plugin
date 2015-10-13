package test.beast.app.seqgen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import beast.app.seqgen.LanguageSequenceGen;
import beast.evolution.alignment.CognateSet;
import beast.evolution.alignment.Language;
import beast.evolution.substitutionmodel.ExplicitBinaryGTR;
import beast.evolution.substitutionmodel.ExplicitBinaryStochasticDollo;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

public class BeastBorrowingPluginTest {
	public static void main(String[] args) {
		run();
	}

	private static void run() {
		// Base Seq generation.
		ArrayList<Integer> seq = new ArrayList<Integer>();
		for (int i = 0; i < 5; i++) {
			seq.add(1);
		}

		//GTRTest(seq);
		//SDTest(seq);
		//TreeGenTest(seq);
		//TreeSDBorrowingTest(seq);
		//TreeGTRBorrowingTest(seq);
		
		SDTreeValidation();

	}

	private static void GTRTest(ArrayList<Integer> seq) {
		Language l = new Language(seq);
		CognateSet c = new CognateSet(l);

		System.out.println("GTR Test");
		System.out.println(l.getLanguage());

		ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5);
		Language gtrLang = gtr_mod.mutateLang(l, c, 10);
		System.out.println(gtrLang.getLanguage());
	}

	private static void SDTest(ArrayList<Integer> seq) {

		Language l2 = new Language(seq);
		CognateSet c = new CognateSet(l2);

		System.out.println("SD Test");
		System.out.println(l2.getLanguage());
		System.out.println(c.getStolloLength());
		ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.5, 0.5);
		Language sdLang = sd_mod.mutateLang(l2, c, 10);
		System.out.println(sdLang.getLanguage());
		System.out.println(c.getStolloLength());
	}

	private static void TreeGenTest(ArrayList<Integer> seq) {
		Language l = new Language(seq);
		CognateSet c = new CognateSet(l);
		ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5);

		System.out.println("Tree generation test");
		System.out.println(c);
		LanguageSequenceGen test = new LanguageSequenceGen();
		Node rootNode = new Node();
		rootNode.setMetaData("lang", c.getLanguage(0));
		rootNode.setHeight(0);
		Tree tree = new Tree(rootNode);
		tree = test.randomTree(tree, 4, 0.6);
		tree.getRoot().setMetaData("lang", c.getLanguage(0));
		gtr_mod.mutateOverTree(tree, c);
		System.out.println();
		System.out.println(c);
	}

	private static void TreeSDBorrowingTest(ArrayList<Integer> seq) {
		Language l = new Language(seq);
		CognateSet c = new CognateSet(l);
		ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.7, 0.3);

		System.out.println("Tree SD Borrowing Test");
		LanguageSequenceGen test = new LanguageSequenceGen();
		Node rootNode = new Node();
		rootNode.setMetaData("lang", c.getLanguage(0));
		rootNode.setHeight(0);
		Tree tree = new Tree(rootNode);
		tree = test.randomTree(tree, 8, 0.6);
		sd_mod.mutateOverTreeBorrowing(tree, c, 1.2, 0.0);
		printTree(tree);
	}
	
	private static void TreeGTRBorrowingTest(ArrayList<Integer> seq) {
		Language l = new Language(seq);
		CognateSet c = new CognateSet(l);
		ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5);

		System.out.println("Tree GTR Borrowing Test");
		LanguageSequenceGen test = new LanguageSequenceGen();
		Node rootNode = new Node();
		rootNode.setMetaData("lang", c.getLanguage(0));
		rootNode.setHeight(0);
		Tree tree = new Tree(rootNode);
		tree = test.randomTree(tree, 8, 0.6);
		gtr_mod.mutateOverTreeBorrowing(tree, c, 1.2, 0.0);
		printTree(tree);
	}
	
	private static void GTRValidation() {
		Integer[] data = new Integer[] {0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 0};
		ArrayList<Integer> births = new ArrayList<Integer>();
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			Language l = new Language(new ArrayList<Integer>(Arrays.asList(data)));
			CognateSet c = new CognateSet(l);
			ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5);
			Language gtrLang = gtr_mod.mutateLang(l, c, 100);
			births.add(gtrLang.getBirths());
		}
		listToCSV(births, "C:/Users/Stuart/Google Drive/University/Year 5 - Honours/Thesis/R_Code/gtr.csv");
	}
	
	private static void SDValidation() {
		ArrayList<Integer> births = new ArrayList<Integer>();
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.5, 0.5);
			Double traits = Randomizer.nextPoisson(sd_mod.getB()/sd_mod.getD());
			ArrayList<Integer> seq = new ArrayList<Integer>();
			for (int j=0; j < traits; j++) {
				seq.add(1);
			}
			Language l = new Language(seq);
			CognateSet c = new CognateSet(l);
			Language sdLang = sd_mod.mutateLang(l, c, 100);
			births.add(sdLang.getBirths());
		}
		listToCSV(births, "C:/Users/Stuart/Google Drive/University/Year 5 - Honours/Thesis/R_Code/sd.csv");
	}
	
	private static void SDTreeValidation() {
		ArrayList<Integer> births = new ArrayList<Integer>();
		
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.5, 0.5);
			Double traits = Randomizer.nextPoisson(sd_mod.getB()/sd_mod.getD());
			ArrayList<Integer> seq = new ArrayList<Integer>();
			for (int j=0; j < traits; j++) {
				seq.add(1);
			}
			Language l = new Language(seq);
			CognateSet c = new CognateSet(l);
			LanguageSequenceGen test = new LanguageSequenceGen();
			Node rootNode = new Node();
			rootNode.setMetaData("lang", c.getLanguage(0));
			rootNode.setHeight(0);
			Tree tree = new Tree(rootNode);
			tree = test.randomTree(tree, 8, 0.6);
			tree = sd_mod.mutateOverTree(tree, c);
			Integer b = 0;
			for (Node n : tree.getExternalNodes()) {
				Language l2 = (Language) n.getMetaData("lang");
				b += l2.getBirths();
			}
			births.add(b);
		}
		//listToCSV(births, "C:/Users/Stuart/Google Drive/University/Year 5 - Honours/Thesis/R_Code/sdtree.csv");
		listToCSV(births, "/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/Utilities/Thesis Graph Generation/sdtree.csv");
	}

	private static void printTree(Tree base) {
		System.out.println("Printing Tree");

		for (Node node : base.listNodesPostOrder(base.getRoot(), null)) {
			System.out.println(((Language) node.getMetaData("lang")).getLanguage());
		}
	}

	private static <T> void listToCSV(ArrayList<T> l, String fileName) {
		final String NEW_LINE_SEPARATOR = "\n";
		FileWriter fW = null;

		try {
			fW = new FileWriter(fileName);
			for ( T i : l) {
				fW.append(String.valueOf(i));
				fW.append(NEW_LINE_SEPARATOR);
			}

		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			try {
				fW.flush();
				fW.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
				e.printStackTrace();
			}

		}
	}
}
