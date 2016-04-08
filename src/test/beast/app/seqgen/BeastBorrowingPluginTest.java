package test.beast.app.seqgen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import beast.app.seqgen.LanguageSequenceGen;
import beast.core.parameter.RealParameter;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Sequence;
import beast.evolution.missingdatamodel.MissingLanguageModel;
import beast.evolution.missingdatamodel.MissingMeaningClassModel;
import beast.evolution.substitutionmodel.ExplicitBinaryGTR;
import beast.evolution.substitutionmodel.ExplicitBinaryStochasticDollo;
import beast.evolution.substitutionmodel.LanguageSubsitutionModel;
import beast.evolution.tree.Node;
import beast.evolution.tree.RandomTree;
import beast.evolution.tree.Tree;
import beast.evolution.tree.TreeUtils;
import beast.evolution.tree.coalescent.ConstantPopulation;
import beast.util.Randomizer;

public class BeastBorrowingPluginTest {
	public static void main(String[] args) throws Exception {
		run();
	}

	private static void run() throws Exception {
		// Base Seq generation.
		String seq = "";
		for (int i = 0; i < 20; i++) {
			seq += '1';
		}
		/*
		 * Sequence test = new Sequence("", "01010");
		 * System.out.println(test.getData()); String newSeq =
		 * LanguageSubsitutionModel.replaceCharAt(test.getData(), 0, "1");
		 * System.out.println(newSeq); test.dataInput.setValue(newSeq, test);
		 * System.out.println(test.getData());
		 */
		
		//System.out.println(randomYuleTree(20,0.00045).toString());
		

		// GTRTest(seq);
		// SDTest(seq);
		// TreeGenTest(seq);
		//TreeSDBorrowingTest(seq);
		//TreeGTRBorrowingTest(seq);
		//countsTest();

		//SDTreeValidation();
		// GTRTreeValidation();
		GTRTreeBorrowingValidationTwoLanguages();
		GTRTreeBorrowingValidationThreeLanguages();
		SDTreeBorrowingValidation();
		// NoEmptyTraitTest();
		// MissingLanguageValidation();
		// MissingMeaningClassesValidation();
		// SpeedTestNonBorrowing();

		 SeqGenTest();
		//misspecGeneration();
		// randomTreeTest();
		//mutationsPerBranches();

	}

	private static void GTRTest(String seq) throws Exception {
		Sequence l = new Sequence("", seq);

		System.out.println("GTR Test");
		System.out.println(l.getData());

		ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5, 0.0, 0.0, false);
		Sequence gtrLang = gtr_mod.mutateLang(l, 10);
		System.out.println(gtrLang.getData());
	}

	private static void SDTest(String seq) throws Exception {

		Sequence l2 = new Sequence("", seq);

		System.out.println("SD Test");
		System.out.println(l2.getData());
		ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.5, 0.5, 0.0, 0.0, false);
		Sequence sdLang = sd_mod.mutateLang(l2, 10);
		System.out.println(sdLang.getData());
	}

	private static void TreeGenTest(String seq) throws Exception {
		Sequence l = new Sequence("", seq);
		ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.5, 0.5, 0.0, 0.0, false);

		System.out.println("Tree generation test");
		Tree tree = randomYuleTree(4, 0.6);
		tree.getRoot().setMetaData("lang", l);
		sd_mod.mutateOverTree(tree);
		for (Node n : tree.getExternalNodes()) {
			Sequence l2 = (Sequence) n.getMetaData("lang");
			System.out.println(l2.getData());
		}
	}

	private static void TreeSDBorrowingTest(String seq) throws Exception {
		seq = "";
		for (int j = 0; j < 20; j++) {
			seq += '1';
		}
		Sequence l = new Sequence("", seq);

		ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.0022314355, 0.00022314355, 0.0, 0.0,
				false);

		System.out.println("Tree SD Borrowing Test");
		Tree tree = randomYuleTree(5, 0.1);
		tree.getRoot().setMetaData("lang", l);
		sd_mod.mutateOverTreeBorrowing(tree);
		for (Node n : tree.getExternalNodes()) {
			Sequence l2 = (Sequence) n.getMetaData("lang");
			System.out.println(l2.getData());
		}
	}

	private static void TreeGTRBorrowingTest(String seq) throws Exception {
		Sequence l = new Sequence("", seq);
		ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5, 1.2, 0.0, false);

		System.out.println("Tree GTR Borrowing Test");
		Tree tree = randomYuleTree(2, 0.01);
		tree.getRoot().setMetaData("lang", l);
		gtr_mod.mutateOverTreeBorrowing(tree);
		for (Node n : tree.getExternalNodes()) {
			Sequence l2 = (Sequence) n.getMetaData("lang");
			System.out.println(l2.getData());
		}
	}

	private static void aliveNodesTest(String seq) throws Exception {
		Sequence l = new Sequence("", seq);
		ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5, 0.5, 0.0, false);

		System.out.println("Tree GTR Borrowing Test");
		Tree tree = randomYuleTree3Branch(3, 0.01);
		tree.getRoot().setMetaData("lang", l);
		gtr_mod.mutateOverTreeBorrowing(tree);
		for (Node n : tree.getExternalNodes()) {
			Sequence l2 = (Sequence) n.getMetaData("lang");
			System.out.println(l2.getData());
		}
	}

	private static void GTRValidation() throws Exception {
		ArrayList<Integer> births = new ArrayList<Integer>();
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			String seq = "";
			for (int j = 0; j < 20; j++) {
				seq += Integer.toString(Randomizer.nextInt(2));
			}
			Sequence l = new Sequence("", seq);
			ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5, 0.0, 0.0, false);
			Sequence gtrLang = gtr_mod.mutateLang(l, 100);
			births.add(LanguageSubsitutionModel.getBirths(gtrLang));
		}
		// listToCSV(births, "C:/Users/Stuart/Google Drive/University/Year 5 -
		// Honours/Thesis/R_Code/gtr.csv");
		listToCSV(births,
				"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr.csv");
	}

	private static void SDValidation() throws Exception {
		ArrayList<Integer> births = new ArrayList<Integer>();
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.5, 0.5, 0.0, 0.0, false);
			String seq = "";
			Sequence l = new Sequence("", seq);
			Sequence sdLang = sd_mod.mutateLang(l, 100);
			births.add(LanguageSubsitutionModel.getBirths(sdLang));
		}
		listToCSV(births,
				"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/sd.csv");
		// listToCSV(births,
		// "/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/Utilities/Thesis
		// Graph Generation/sd.csv");

	}

	private static void GTRTreeValidation() throws Exception {
		ArrayList<Integer> births = new ArrayList<Integer>();
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5, 0.0, 0.0, false);
			String seq = "";
			for (int j = 0; j < 20; j++) {
				seq += Integer.toString(Randomizer.nextInt(2));
			}
			Sequence l = new Sequence("", seq);
			Tree tree = randomYuleTree(8, 0.6);
			tree.getRoot().setMetaData("lang", l);
			tree = gtr_mod.mutateOverTree(tree);
			for (Node n : tree.getExternalNodes()) {
				Sequence l2 = (Sequence) n.getMetaData("lang");
				births.add(LanguageSubsitutionModel.getBirths(l2));
			}

		}
		listToCSV(births,
				"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtrtree.csv");
		// listToCSV(births,
		// "/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/Utilities/Thesis
		// Graph Generation/sdtree.csv");
	}

	private static void SDTreeValidation() throws Exception {
		ArrayList<Integer> births = new ArrayList<Integer>();

		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.5, 0.5, 0.0, 0.0, false);
			String seq = "";
			Sequence l = new Sequence("", seq);
			Tree tree = randomYuleTree(8, 0.01);
			tree.getRoot().setMetaData("lang", l);
			tree = sd_mod.mutateOverTree(tree);
			for (Node n : tree.getExternalNodes()) {
				Sequence l2 = (Sequence) n.getMetaData("lang");
				births.add(LanguageSubsitutionModel.getBirths(l2));
			}
		}
		listToCSV(births,
				"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/sdtree.csv");
		// listToCSV(births,
		// "/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/Utilities/Thesis
		// Graph Generation/sdtree.csv");
	}

	private static void GTRTreeBorrowingValidationTwoLanguages() throws Exception {
		Tree tree = null;
		ArrayList<Integer> zeroZero = new ArrayList<Integer>();
		ArrayList<Integer> zeroOne = new ArrayList<Integer>();
		ArrayList<Integer> oneZero = new ArrayList<Integer>();
		ArrayList<Integer> oneOne = new ArrayList<Integer>();
		for (int i = 0; i < 10000; i++) {
			System.out.println(i);
			ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5, 0.5, 0.0, false);
			String seq = "";
			for (int j = 0; j < 20; j++) {
				seq += Integer.toString(Randomizer.nextInt(2));
			}
			Sequence l = new Sequence("", seq);
			tree = randomYuleTree(2, 0.01);
			tree.getRoot().setMetaData("lang", l);
			tree = gtr_mod.mutateOverTreeBorrowing(tree);
			List<Node> ext = tree.getRoot().getChildren();
			String l1 = ((Sequence) ext.get(0).getMetaData("lang")).getData();
			String l2 = ((Sequence) ext.get(1).getMetaData("lang")).getData();
			int zeroZeroInt = 0;
			int zeroOneInt = 0;
			int oneZeroInt = 0;
			int oneOneInt = 0;
			for (int j = 0; j < 20; j++) {
				if (l1.charAt(j) == '0' && l2.charAt(j) == '0') {
					zeroZeroInt += 1;
				} else if (l1.charAt(j) == '0' && l2.charAt(j) == '1') {
					zeroOneInt += 1;
				} else if (l1.charAt(j) == '1' && l2.charAt(j) == '0') {
					oneZeroInt += 1;
				} else {
					oneOneInt += 1;
				}
			}
			zeroZero.add(zeroZeroInt);
			zeroOne.add(zeroOneInt);
			oneZero.add(oneZeroInt);
			oneOne.add(oneOneInt);
		}
		

		String absPath = "/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/";
		//absPath = "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/";

		
		listToCSV(zeroZero,
				absPath + "Utilities/Thesis Graph Generation/gtr00.csv");
		listToCSV(zeroOne,
				absPath + "Utilities/Thesis Graph Generation/gtr01.csv");
		listToCSV(oneZero,
				absPath + "Utilities/Thesis Graph Generation/gtr10.csv");
		listToCSV(oneOne,
				absPath + "Utilities/Thesis Graph Generation/gtr11.csv");
	}

	private static void GTRTreeBorrowingValidationThreeLanguages() throws Exception {
		Tree tree = null;
		ArrayList<Integer> zeroZeroZero = new ArrayList<Integer>();
		ArrayList<Integer> oneZeroZero = new ArrayList<Integer>();
		ArrayList<Integer> zeroOneZero = new ArrayList<Integer>();
		ArrayList<Integer> zeroZeroOne = new ArrayList<Integer>();
		ArrayList<Integer> oneOneZero = new ArrayList<Integer>();
		ArrayList<Integer> oneZeroOne = new ArrayList<Integer>();
		ArrayList<Integer> zeroOneOne = new ArrayList<Integer>();
		ArrayList<Integer> oneOneOne = new ArrayList<Integer>();
		for (int i = 0; i < 10000; i++) {
			System.out.println(i);
			ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5, 0.5, 0.0, false);
			String seq = "";
			for (int j = 0; j < 20; j++) {
				seq += Integer.toString(Randomizer.nextInt(2));
			}
			Sequence l = new Sequence("", seq);
			tree = randomYuleTree3Branch(3, 0.01);
			tree.getRoot().setMetaData("lang", l);
			tree = gtr_mod.mutateOverTreeBorrowing(tree);
			List<Node> ext = tree.getExternalNodes();
			String l1 = ((Sequence) ext.get(0).getMetaData("lang")).getData();
			String l2 = ((Sequence) ext.get(1).getMetaData("lang")).getData();
			String l3 = ((Sequence) ext.get(2).getMetaData("lang")).getData();
			Integer zeroZeroZeroInt = 0;
			Integer oneZeroZeroInt = 0;
			Integer zeroOneZeroInt = 0;
			Integer zeroZeroOneInt = 0;
			Integer oneOneZeroInt = 0;
			Integer oneZeroOneInt = 0;
			Integer zeroOneOneInt = 0;
			Integer oneOneOneInt = 0;
			for (int j = 0; j < 20; j++) {
				if (l1.charAt(j) == '0' && l2.charAt(j) == '0' && l3.charAt(j) == '0') {
					zeroZeroZeroInt += 1;
				} else if (l1.charAt(j) == '1' && l2.charAt(j) == '0' && l3.charAt(j) == '0') {
					oneZeroZeroInt += 1;
				} else if (l1.charAt(j) == '0' && l2.charAt(j) == '1' && l3.charAt(j) == '0') {
					zeroOneZeroInt += 1;
				} else if (l1.charAt(j) == '0' && l2.charAt(j) == '0' && l3.charAt(j) == '1') {
					zeroZeroOneInt += 1;
				} else if (l1.charAt(j) == '1' && l2.charAt(j) == '1' && l3.charAt(j) == '0') {
					oneOneZeroInt += 1;
				} else if (l1.charAt(j) == '1' && l2.charAt(j) == '0' && l3.charAt(j) == '1') {
					oneZeroOneInt += 1;
				} else if (l1.charAt(j) == '0' && l2.charAt(j) == '1' && l3.charAt(j) == '1') {
					zeroOneOneInt += 1;
				} else if (l1.charAt(j) == '1' && l2.charAt(j) == '1' && l3.charAt(j) == '1') {
					oneOneOneInt += 1;
				}
			}
			zeroZeroZero.add(zeroZeroZeroInt);
			oneZeroZero.add(oneZeroZeroInt);
			zeroOneZero.add(zeroOneZeroInt);
			zeroZeroOne.add(zeroZeroOneInt);
			oneOneZero.add(oneOneZeroInt);
			oneZeroOne.add(oneZeroOneInt);
			zeroOneOne.add(zeroOneOneInt);
			oneOneOne.add(oneOneOneInt);
		}
		
		String absPath = "/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/";
		//absPath = "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/";

		listToCSV(zeroZeroZero,
				absPath + "Utilities/Thesis Graph Generation/gtr000.csv");
		listToCSV(oneZeroZero,
				absPath + "Utilities/Thesis Graph Generation/gtr100.csv");
		listToCSV(zeroOneZero,
				absPath + "Utilities/Thesis Graph Generation/gtr010.csv");
		listToCSV(zeroZeroOne,
				absPath + "Utilities/Thesis Graph Generation/gtr001.csv");
		listToCSV(oneOneZero,
				absPath + "Utilities/Thesis Graph Generation/gtr110.csv");
		listToCSV(oneZeroOne,
				absPath + "Utilities/Thesis Graph Generation/gtr101.csv");
		listToCSV(zeroOneOne,
				absPath + "Utilities/Thesis Graph Generation/gtr011.csv");
		listToCSV(oneOneOne,
				absPath + "Utilities/Thesis Graph Generation/gtr111.csv");

	}

	private static void SDTreeBorrowingValidation() throws Exception {
		Tree tree = null;
		ArrayList<Integer> births = new ArrayList<Integer>();
		for (int i = 0; i < 1000; i++) {
			System.out.println(i);
			ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.0022314355, 0.00022314355, 0.1,
					0.0, false);
			String seq = "";
			Integer pos = (int) Randomizer.nextPoisson(10.0);
			for (int j = 0; j < pos; j++) {
				seq += '1';
			}
			Sequence l = new Sequence("", seq);
			Node rootNode = new Node();
			rootNode.setMetaData("lang", l);
			tree = randomYuleTree(8, 0.1);
			tree = new Tree(tree.toString());
			tree.getRoot().setMetaData("lang", l);
			tree = sd_mod.mutateOverTreeBorrowing(tree);
			for (Node n : tree.getNodesAsArray()) {
				if (n.isLeaf()) {
					Sequence l2 = (Sequence) n.getMetaData("lang");
					births.add(LanguageSubsitutionModel.getBirths(l2));
				}
			}
		}
		

		String absPath = "/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/";
		//absPath = "C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/";

		
		listToCSV(births,
				absPath + "Utilities/Thesis Graph Generation/sdtreeborrowing.csv");
	}

	private static void SpeedTestNonBorrowing() throws Exception {
		ArrayList<Long> GTRA1 = new ArrayList<Long>();
		ArrayList<Long> GTRA2 = new ArrayList<Long>();
		ArrayList<Long> SDA1 = new ArrayList<Long>();
		ArrayList<Long> SDA2 = new ArrayList<Long>();
		ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.5, 0.5, 0.0, 0.0, false);
		ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5, 0.0, 0.0, false);
		Tree tree = null;
		long startTime, endTime;
		for (int i = 0; i < 10000; i++) {
			System.out.println(i);
			String seq = "";
			Integer pos = (int) Randomizer.nextPoisson(10.0);
			for (int j = 0; j < pos; j++) {
				seq += '1';
			}
			Sequence l = new Sequence("", seq);
			tree = randomYuleTree(8, 0.06);
			tree.getRoot().setMetaData("lang", l);

			startTime = System.nanoTime();
			tree = gtr_mod.mutateOverTree(tree);
			endTime = System.nanoTime();
			GTRA1.add((endTime - startTime) / 1000000);

			startTime = System.nanoTime();
			tree = gtr_mod.mutateOverTreeBorrowing(tree);
			endTime = System.nanoTime();
			GTRA2.add((endTime - startTime) / 1000000);

			startTime = System.nanoTime();
			tree = sd_mod.mutateOverTree(tree);
			endTime = System.nanoTime();
			SDA1.add((endTime - startTime) / 1000000);

			startTime = System.nanoTime();
			tree = sd_mod.mutateOverTreeBorrowing(tree);
			endTime = System.nanoTime();
			SDA2.add((endTime - startTime) / 1000000);

			listToCSV(GTRA1,
					"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/speed_gtr_a1.csv");
			listToCSV(GTRA2,
					"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/speed_gtr_a2.csv");
			listToCSV(SDA1,
					"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/speed_sd_a1.csv");
			listToCSV(SDA2,
					"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/speed_sd_a2.csv");
		}
	}

	private static void countsTest() throws Exception {
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5, 0.0, 0.0, false);
		for (int i = 0; i < 1; i++) {
			String seq = "";
			for (int j = 0; j < 100; j++) {
				seq += Integer.toString(Randomizer.nextInt(2));
			}
			Sequence l = new Sequence("", seq);
			Tree tree = randomYuleTree3Branch(3, 0.01);
			tree.getRoot().setMetaData("lang", l);
			HashMap<String, Integer> events = gtr_mod.mutateOverTreeBorrowingWithEvents(tree);
			results.putAll(events);
		}
		Double total = results.get("total") + 0.0;
		for (String name : results.keySet()) {
			String key = name.toString();
			if (key != "total") {
				Integer value = results.get(name);
				System.out.println(key + " " + value / total);
			}
		}
	}

	private static void SeqGenTest() throws Exception {
		//String[] args = {
				//"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/examples/BorrowingMisspec/SD_Borrow_0_Input.xml",
				//"1",
				//"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/examples/BorrowingMisspec/Outputs/SD_Borrow_0_Output.xml" };
		String[] args = {
				"/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/examples/BorrowingMisspec/GTR_Borrow_1_Input.xml",
				"1",
				"/home/stuart/Code/Beast2-plugin/Beast-Borrowing-Plugin/examples/BorrowingMisspec/Outputs/GTR_Borrow_1_Output.xml"};
		
		Long startTime = System.nanoTime();
		LanguageSequenceGen.main(args);
		Long endTime = System.nanoTime();
		System.out.println();
		System.out.println("Time: "+ (endTime - startTime) / 1000000);
		
	}
	
	private static void misspecGeneration() throws Exception {
		int[] borrowingRates = {0,1,5,10,15,20,30,40,50};
		String[] models = {"SD","GTR"};
		for (String model : models) {
			System.out.println(model);
			for (int b : borrowingRates) {
				long startTime = System.currentTimeMillis();
				System.out.println(b);
				System.out.println();
				String[] args = {
						"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/examples/BorrowingMisspec/"+model+ "_Borrow_"+b+"_Input.xml",
						"1",
						"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/examples/BorrowingMisspec/Outputs/"+model+"_Borrow_"+b+"_Output.xml" 
				};
				LanguageSequenceGen.main(args);
				long endTime = System.currentTimeMillis();
				System.out.println("That took " + (endTime - startTime) + " milliseconds");
			}
		}
	}

	private static void randomTreeTest() throws Exception {
		StringBuilder traitSB = new StringBuilder();
		List<Sequence> seqList = new ArrayList<Sequence>();

		for (int i = 0; i < 10; i++) {
			String taxonID = "t " + i;
			seqList.add(new Sequence(taxonID, "?"));

			if (i > 0)
				traitSB.append(",");
			traitSB.append(taxonID).append("=").append(i);
		}

		Alignment alignment = new Alignment(seqList, "nucleotide");
		ConstantPopulation popFunc = new ConstantPopulation();
		popFunc.initByName("popSize", new RealParameter("1.0"));
		RandomTree t = new RandomTree();
		t.initByName("taxa", alignment, "populationModel", popFunc);

		Sequence l = new Sequence("", "");

		System.out.println("Tree GTR Borrowing Test");
		Tree tree = randomYuleTree(2, 0.01);
		tree.getRoot().setMetaData("lang", l);
		System.out.println(TreeUtils.getTreeLength(tree, tree.getRoot()));

	}

	private static void NoEmptyTraitTest() throws Exception {
		for (int i = 0; i < 1; i++) {
			System.out.println(i);
			Sequence l = new Sequence("", "00000000000001");
			Tree tree = randomYuleTree(3, 0.06);
			tree.getRoot().setMetaData("lang", l);
			ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.0, 0.5, 0.0, 0.0, true);
			tree = sd_mod.mutateOverTree(tree);

			for (Node n : tree.getExternalNodes()) {
				Sequence l2 = (Sequence) n.getMetaData("lang");
				System.out.println(l2.getData());
			}

		}
	}

	private static void MissingMeaningClassesValidation() throws Exception {
		ArrayList<Integer> missingMCs = new ArrayList<Integer>();
		MissingMeaningClassModel model_mc = new MissingMeaningClassModel(0.5);
		for (int i = 0; i < 100000; i++) {
			System.out.println(i);
			ArrayList<Sequence> test = new ArrayList<Sequence>();
			String seq = "";
			for (int j = 0; j < 100; j++) {
				seq += "1";
			}

			for (int j = 0; j < 10; j++) {
				Sequence l = new Sequence(Integer.toString(j), seq);
				test.add(l);

			}
			test.add(new Sequence("mc", "0 10 20 30 40 50 60 70 80 90"));

			ArrayList<Sequence> tmp = new ArrayList<Sequence>();
			tmp = model_mc.generateMissingData(test);

			int missingMC = 0;
			char[] seqArray = tmp.get(0).getData().toCharArray();
			for (int j = 0; j < 100; j += 10) {
				if (seqArray[j] == '?') {
					missingMC += 1;
				}
			}
			missingMCs.add(missingMC);
		}
		listToCSV(missingMCs,
				"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/missing_mc2.csv");
	}

	private static void MissingLanguageValidation() throws Exception {
		ArrayList<Integer> missingLangs = new ArrayList<Integer>();

		MissingLanguageModel model_lang = new MissingLanguageModel(0.5);
		for (int i = 0; i < 100000; i++) {
			ArrayList<Sequence> testLang = new ArrayList<Sequence>();
			String seq = "";
			for (int j = 0; j < 100; j++) {
				seq += "1";
			}

			for (int j = 0; j < 10; j++) {
				Sequence l = new Sequence(Integer.toString(j), seq);
				testLang.add(l);

			}
			testLang.add(new Sequence("mc", "0 10 20 30 40 50 60 70 80 90"));
			System.out.println(i);
			ArrayList<Sequence> tmpLang = new ArrayList<Sequence>();
			tmpLang = model_lang.generateMissingData(testLang);
			int missingLang = 0;
			for (Sequence t : tmpLang) {
				if (t.getData().contains("?")) {
					missingLang += 1;
				}
			}
			missingLangs.add(missingLang);
		}

		listToCSV(missingLangs,
				"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/missing_lang.csv");
	}
	
	private static void mutationsPerBranches() throws Exception {
		Tree tr = new Tree("((0:100,1:100)2:100,(3:100,4:100)5:100)6:0.0");
			ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5, 0.0, 0.0, false);
			String seq = "";
			for (int j = 0; j < 20; j++) {
				seq += Integer.toString(Randomizer.nextInt(2));
			}
			Sequence l = new Sequence("", seq);
			tr.getRoot().setMetaData("lang", l);
			HashMap<String, Integer> events = gtr_mod.mutateOverTreeBorrowingWithEvents(tr);
			
		for (String name : events.keySet()) {
			System.out.println(name + ": " + events.get(name));
		}
	}

	private static <T> void listToCSV(ArrayList<T> l, String fileName) {
		final String NEW_LINE_SEPARATOR = "\n";
		FileWriter fW = null;

		try {
			fW = new FileWriter(fileName);
			for (T i : l) {
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
	
	private static Tree randomYuleTree (int nodes, double l) throws Exception {
		Tree tr = new Tree();
		ArrayList<Node> nodeList = new ArrayList<Node>();
		double t = 0.0;
		int label = 1;
		for (int i = 0; i < nodes; i++) {
			Node n = new Node();
			n.setHeight(t);
			n.setNr(label);
			label++;
			nodeList.add(n);
		}
		
		while (nodeList.size() > 1) {
			t += Randomizer.nextExponential(nodeList.size()*l);
			
			int p_1_index = Randomizer.nextInt(nodeList.size());
			Node p1 = nodeList.remove(p_1_index);
			int p_2_index = Randomizer.nextInt(nodeList.size());
			Node p2 = nodeList.remove(p_2_index);
			
			Node parent = new Node();
			parent.setHeight(t);

			parent.setNr(label);
			label++;
			p1.setParent(parent);
			parent.addChild(p1);
			p2.setParent(parent);
			parent.addChild(p2);
			
			nodeList.add(parent);
		}
		tr.setRoot(nodeList.get(0));
		return new Tree(tr.toString());
		
	}
	
	private static Tree randomYuleTree3Branch (int nodes, double l) throws Exception {
		return new Tree("(0:100,1:100,2:100)3:0.0");	
	}

}
