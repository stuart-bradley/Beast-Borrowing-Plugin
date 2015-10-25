package test.beast.app.seqgen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
		
		//GTRTreeBorrowingValidation();
		//SDTreeValidation();
		
		SeqGenTest();


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
	}
	
	private static void GTRValidation() {
		ArrayList<Integer> births = new ArrayList<Integer>();
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			ArrayList<Integer> seq = new ArrayList<Integer>();
			for (int j = 0; j < 20; j++) {
				seq.add(Randomizer.nextInt(2));
			}
			Language l = new Language(seq);
			CognateSet c = new CognateSet(l);
			ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5);
			Language gtrLang = gtr_mod.mutateLang(l, c, 100);
			births.add(gtrLang.getBirths());
		}
		//listToCSV(births, "C:/Users/Stuart/Google Drive/University/Year 5 - Honours/Thesis/R_Code/gtr.csv");
		listToCSV(births, "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr.csv");
	}
	
	private static void SDValidation() {
		ArrayList<Integer> births = new ArrayList<Integer>();
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.5, 0.5);
			ArrayList<Integer> seq = new ArrayList<Integer>();
			Language l = new Language(seq);
			CognateSet c = new CognateSet(l);
			Language sdLang = sd_mod.mutateLang(l, c, 100);
			births.add(sdLang.getBirths());
		}
		listToCSV(births, "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/sd.csv");
		//listToCSV(births, "/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/Utilities/Thesis Graph Generation/sd.csv");

	}
	
	private static void GTRTreeValidation() {
		ArrayList<Integer> births = new ArrayList<Integer>();
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5);
			ArrayList<Integer> seq = new ArrayList<Integer>();
			for (int j = 0; j < 20; j++) {
				seq.add(Randomizer.nextInt(2));
			}
			Language l = new Language(seq);
			CognateSet c = new CognateSet(l);
			LanguageSequenceGen test = new LanguageSequenceGen();
			Node rootNode = new Node();
			rootNode.setMetaData("lang", c.getLanguage(0));
			rootNode.setHeight(0);
			Tree tree = new Tree(rootNode);
			tree = test.randomTree(tree, 8, 0.6);
			tree = gtr_mod.mutateOverTree(tree, c);
			for (Node n : tree.getExternalNodes()) {
				Language l2 = (Language) n.getMetaData("lang");
				births.add(l2.getBirths());
			}
			
		}
		listToCSV(births, "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtrtree.csv");
		//listToCSV(births, "/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/Utilities/Thesis Graph Generation/sdtree.csv");
	}
	
	private static void SDTreeValidation() {
		ArrayList<Integer> births = new ArrayList<Integer>();
		
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.5, 0.5);
			ArrayList<Integer> seq = new ArrayList<Integer>();
			Language l = new Language(seq);
			CognateSet c = new CognateSet(l);
			LanguageSequenceGen test = new LanguageSequenceGen();
			Node rootNode = new Node();
			rootNode.setMetaData("lang", c.getLanguage(0));
			rootNode.setHeight(0);
			Tree tree = new Tree(rootNode);
			tree = test.randomTree(tree, 8, 0.005);
			tree = sd_mod.mutateOverTree(tree, c);
			for (Node n : tree.getExternalNodes()) {
				Language l2 = (Language) n.getMetaData("lang");
				births.add(l2.getBirths());
			}
			
		}
		listToCSV(births, "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/sdtree.csv");
		//listToCSV(births, "/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/Utilities/Thesis Graph Generation/sdtree.csv");
	}
	
	private static void GTRTreeBorrowingValidation() {
		Tree tree = null;
		ArrayList<Integer> zeroZero = new ArrayList<Integer>();
		ArrayList<Integer> zeroOne = new ArrayList<Integer>();
		ArrayList<Integer> oneZero = new ArrayList<Integer>();
		ArrayList<Integer> oneOne = new ArrayList<Integer>();
		ArrayList<Integer> all = new ArrayList<Integer>();
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5);
			ArrayList<Integer> seq = new ArrayList<Integer>();
			for (int j = 0; j < 20; j++) {
				seq.add(Randomizer.nextInt(2));
			}
			Language l = new Language(seq);
			CognateSet c = new CognateSet(l);
			LanguageSequenceGen test = new LanguageSequenceGen();
			Node rootNode = new Node();
			rootNode.setMetaData("lang", c.getLanguage(0));
			rootNode.setHeight(0);
			tree = new Tree(rootNode);
			tree = test.randomTree(tree, 2, 0.1);
			tree = gtr_mod.mutateOverTreeBorrowing(tree, c, 0.5, 0.0);
			List<Node> ext = tree.getExternalNodes();
			ArrayList<Integer> l1 = ((Language) ext.get(0).getMetaData("lang")).getLanguage();
			ArrayList<Integer> l2 = ((Language) ext.get(1).getMetaData("lang")).getLanguage();
			int zeroZeroInt = 0;
			int zeroOneInt = 0;
			int oneZeroInt = 0;
			int oneOneInt = 0;
			for (int j = 0; j < 20; j++) {
				if (l1.get(j) == 0 && l2.get(j) == 0) {
					zeroZeroInt += 1;
				} else if (l1.get(j) == 0 && l2.get(j) == 1) {
					zeroOneInt += 1;
				} else if (l1.get(j) == 1 && l2.get(j) == 0) {
					oneZeroInt += 1;
				} else {
					oneOneInt += 1;
				}
			}
			zeroZero.add(zeroZeroInt);
			all.add(zeroZeroInt);
			zeroOne.add(zeroOneInt);
			all.add(zeroOneInt);
			oneZero.add(oneZeroInt);
			all.add(oneZeroInt);
			oneOne.add(oneOneInt);
			all.add(oneOneInt);
			
			
		}
		listToCSV(all, "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/all.csv");
		listToCSV(zeroZero, "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr00.csv");
		listToCSV(zeroOne, "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr01.csv");
		listToCSV(oneZero, "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr10.csv");
		listToCSV(oneOne, "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr11.csv");
	}
	
	private static void SeqGenTest() {
		String[] args = {"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/testLangSeqGen.xml","100","C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/output.xml"};
		LanguageSequenceGen.main(args);
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
