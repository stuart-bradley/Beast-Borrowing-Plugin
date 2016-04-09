package test.beast.app.seqgen;

import java.util.ArrayList;
import java.util.Random;

import beast.evolution.alignment.Sequence;
import beast.evolution.substitutionmodel.ExplicitBinaryGTR;
import beast.evolution.substitutionmodel.ExplicitBinaryStochasticDollo;
import beast.evolution.tree.Node;

public class SDRatesTest {
	public static void main(String[] args) throws Exception {
		SDTest(0.5,0.5, 0.0);

	}
	
	protected static void SDTest(double b, double d, double br) throws Exception {
		Random rand = new Random();

		for (int i = 0; i < 100; i++) {
			Double javaRate = 0.0;
			Double simRate = 0.0;
			System.out.println();
			//ArrayList<Node> aliveNodes = determineInput();
			ArrayList<Node> aliveNodes = new ArrayList<Node>();
			for (int j = 0; j < 3; j++) {
				Node n = new Node();
				String lang = "";
				for (int k = 0; k < 5; k++) {
					lang += 0 + rand.nextInt((1 - 0) + 1);
				}
				System.out.println(lang);
				n.setMetaData("lang", new Sequence("", lang));
				aliveNodes.add(n);
			}
			
			System.out.println();
			System.out.println("Simulation calc:");
			Double deathsum = 0.0;
			Double borrowsum = 0.0;
			for (Node n : aliveNodes) {
				deathsum += d*getBirths(getSequence(n));
				borrowsum += getBirths(getSequence(n));
			}
			simRate = (b*3) + deathsum + (br*borrowsum);
			System.out.println("Simulation total rate = " + simRate);
			
			System.out.println();
			System.out.println("In code calc: ");
			ExplicitBinaryStochasticDollo sd = new ExplicitBinaryStochasticDollo(b,d,br,0.0,false);
			String[]stringAliveNodes = getSequences(aliveNodes);
			javaRate = sd.totalRate(stringAliveNodes);
			System.out.println("In code total rate = " + javaRate);
			
			if (! javaRate.equals(simRate)) {
				break;
			}
		}
	}
	
	protected static String[] getSequences(ArrayList<Node> aliveNodes) {
		int aNSize = aliveNodes.size();
		String[] seqs = new String[aNSize];
		for (int i = 0; i < aNSize; i++) {
			seqs[i] = ((Sequence) aliveNodes.get(i).getMetaData("lang")).getData();
		}
		return seqs;
	}
	
	protected static Sequence getSequence(Node n) throws Exception {
		try {
			return (Sequence) n.getMetaData("lang");
		} catch (ClassCastException e) {
			return new Sequence(n.metaDataString, "");
		}
	}

	protected static int getBirths(Sequence l) {
		String seq = l.getData();
		int count = 0;
		for (char c : seq.toCharArray()) {
			if (Character.getNumericValue(c) == 1) {
				count += 1;
			}
		}
		return count;
	}
}
