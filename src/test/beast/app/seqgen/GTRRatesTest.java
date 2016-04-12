package test.beast.app.seqgen;

import java.util.ArrayList;
import java.util.Random;

import beast.evolution.alignment.Sequence;
import beast.evolution.substitutionmodel.ExplicitBinaryGTR;
import beast.evolution.tree.Node;

public class GTRRatesTest {

	public static void main(String[] args) throws Exception {
		threeLangTest(0.5,0.5);

	}
	
	protected static void threeLangTest(double m, double b) throws Exception {
		Random rand = new Random();

		for (int i = 0; i < 100; i++) {
			Double javaRate = 0.0;
			Double mathRate = 0.0;
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
			System.out.println("Rate matrix calc:");
			for (int j = 0; j < 5; j++) {
				mathRate += getMatrixResult(getPositionState(aliveNodes, j), m, b);
			}
			System.out.println("Rate matrix total rate = " + mathRate);
			
			System.out.println();
			System.out.println("Simulation calc:");
			Double p = births(aliveNodes);
			simRate = (m*(3*5) + b * m * p);
			System.out.println("Simulation total rate = " + simRate);
			
			System.out.println();
			System.out.println("In code calc: ");
			ExplicitBinaryGTR gtr = new ExplicitBinaryGTR(m,b,0.0,false);
			String[]stringAliveNodes = getSequences(aliveNodes);
			int[] traits = getBirths(stringAliveNodes, aliveNodes.size());
			javaRate = gtr.totalRate(stringAliveNodes, traits, aliveNodes.size());
			System.out.println("In code total rate = " + javaRate);
			
			if (! javaRate.equals(simRate) || ! javaRate.equals(mathRate) || ! simRate.equals(mathRate)) {
				break;
			}
		}
		
	}
	

	protected static Double totalRate(ArrayList<Node> aliveNodes, double rate, double borrowRate) throws Exception {
		Double borrowSum = 0.0;
		Double mutateSum = 0.0;
		for (Node n : aliveNodes) {
			borrowSum += getBirths(getSequence(n));
			mutateSum += (getSequence(n)).getData().length();
		}
		return rate * mutateSum + borrowRate * rate * borrowSum;
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
	
	protected static String getPositionState(ArrayList<Node> aliveNodes, int pos) {
		String res = "";
		for (Node n : aliveNodes) {
			res += ((Sequence) n.getMetaData("lang")).getData().charAt(pos);
		}
		return res;
	}
	
	protected static double getMatrixResult(String t, double m, double b) {
		if (t.equals("000")|| t.equals("111")) {
			System.out.println(t + ": " + "m + m + m" );
			return m + m + m; 
		} else if (t.equals("100") || t.equals("010") || t.equals("001")) {
			System.out.println(t + ": " + "m + m*(b/2+1) + m*(b/2+1)");
			return m + m*(b/2.0+1) + m*(b/2.0+1.0);
		} else {
			System.out.println(t + ": " + "m + m + (b+1.0)*m");
			return m + m + (b+1.0)*m;
		}
	}
	
	protected static double births(ArrayList<Node> aliveNodes) {
		double births = 0.0;
		for (Node n : aliveNodes) {
			births += getBirths((Sequence) n.getMetaData("lang"));
		}
		return births;
	}

	
	protected static ArrayList<Node> determineInput() throws Exception {
		String[] langs = {"10101", "11111", "10101"};
		ArrayList<Node> aliveNodes = new ArrayList<Node>();
		for (int i = 0; i < 3; i++) {
			Node n = new Node();
			n.setMetaData("lang", new Sequence("", langs[i]));
			System.out.println(langs[i]);
			aliveNodes.add(n);
		}
		return aliveNodes;
	}
	
	protected static String[] getSequences(ArrayList<Node> aliveNodes) {
		int aNSize = aliveNodes.size();
		String[] seqs = new String[aNSize];
		for (int i = 0; i < aNSize; i++) {
			seqs[i] = ((Sequence) aliveNodes.get(i).getMetaData("lang")).getData();
		}
		return seqs;
	}
	
	protected static int[] getBirths(String[] aliveNodes, int numberOfLangs) {
		int[] births = new int[numberOfLangs];
		for (int i =0; i < numberOfLangs; i++) {
			births[i] = (int) aliveNodes[i].chars().filter(ch -> ch =='1').count();
		}
		return births;
	}
		
}
