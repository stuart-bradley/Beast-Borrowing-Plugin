import java.util.ArrayList;

import beast.app.seqgen.LanguageSequenceGen;
import beast.evolution.alignment.CognateSet;
import beast.evolution.alignment.Language;
import beast.evolution.substitutionmodel.ExplicitBinaryGTR;
import beast.evolution.substitutionmodel.ExplicitBinaryStochasticDollo;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

public class TestingClass {
	public static void main(String [ ] args){
		run();
	}
	
	private static void run() {
		ArrayList<Integer> seq = new ArrayList<Integer>();
		for (int i = 0; i < 5; i++) {
			seq.add(1);
		}
		Language l = new Language(seq);
		CognateSet c = new CognateSet(l);
		
		System.out.println("GTR Test");
		System.out.println(l.getLanguage());
		ExplicitBinaryGTR gtr_mod = new ExplicitBinaryGTR(0.5, 0.5);
		Language gtrLang = gtr_mod.mutate_GTR(l, 10);
		System.out.println(gtrLang.getLanguage());
		
		System.out.println("SD Test");
		Language l2 = new Language(seq);
		c = new CognateSet(l2);
		System.out.println(l2.getLanguage());
		System.out.println(c.getStolloLength());
		ExplicitBinaryStochasticDollo sd_mod = new ExplicitBinaryStochasticDollo(0.5, 0.5);
		Language sdLang = sd_mod.mutate_SD(l2,c, 10);
		System.out.println(sdLang.getLanguage());
		System.out.println(c.getStolloLength());
		
		System.out.println("Tree generation test");
		c = new CognateSet(l);
		System.out.println(c);
		LanguageSequenceGen test = new LanguageSequenceGen();
		Node rootNode = new Node();
		rootNode.setMetaData("language", c.getLanguage(0));
		rootNode.setHeight(0);
		Tree tree = new Tree(rootNode);
		tree = test.randomTree(tree, 4, 0.6);
		tree.getRoot().setMetaData("lang", c.getLanguage(0));
		gtr_mod.mutateOverTree(tree, c);
		System.out.println();
		System.out.println(c);
	}
}
