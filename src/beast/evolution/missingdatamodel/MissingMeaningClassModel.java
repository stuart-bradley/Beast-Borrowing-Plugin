package beast.evolution.missingdatamodel;

import java.util.ArrayList;

import beast.core.Input;
import beast.evolution.alignment.Sequence;
import beast.util.Randomizer;

/*
 * MissingMeaningClassModel Class
 * 
 * Determines which meaning classes appear unknown in the final alignment.
 * Does this by virtue of a simple coin-flip for each meaning class.
 * 
 * @author Stuart Bradley (sbra886@aucklanduni.ac.nz)
 * @version 1.0
 * 
 */

public class MissingMeaningClassModel extends MissingDataModel {
	public Input<Double> rateInput = new Input<Double>("rate", "missing language rate");
	
	/** rate */
	protected double rate;
	
	public MissingMeaningClassModel() throws Exception {
	}
	
	public MissingMeaningClassModel(double r) {
		this.setRate(r);
	}

	@Override
	public void initAndValidate() {
		this.rate = rateInput.get();
	}
	
	@Override
	public ArrayList<Sequence> generateMissingData(ArrayList<Sequence> a) throws Exception {
		Sequence meaningClasses = a.get(a.size()-1);
		String[] mcArray = meaningClasses.getData().split(" ");
		ArrayList<Integer> meaningClassesList = new ArrayList<Integer>();
		for (String s : mcArray) {
			meaningClassesList.add(Integer.parseInt(s));
		}
		for (int mc : meaningClassesList) {
			if (Randomizer.nextDouble() <= rate) {
				a = meaningClassToUnknown(a, mc, meaningClassesList);
			}
		}
		return a;
	}
	
	private ArrayList<Sequence> meaningClassToUnknown(ArrayList<Sequence> a, int startIndex, ArrayList<Integer> mcList) throws Exception {
		int mcIndex = mcList.indexOf(startIndex);
		// MC is last in list.
		int endIndex = a.get(0).getData().length();
		if (mcIndex != mcList.size()-1) {
			endIndex = mcList.get(mcIndex+1);
		}
		
		String unknown = "";
		
		for (int i = startIndex; i < endIndex; i++) {
			unknown += "?";
		}
		
		for (int i = 0; i < a.size()-1; i++) {
			Sequence s = a.get(i);
			String taxa = s.getTaxon();
			String newData = replaceStringAt(s.getData(), unknown, startIndex, endIndex);
			a.set(i, new Sequence(taxa, newData));
		}
		return a;	
	}
	
	/*
	 * Replaces string (c) in string (s).
	 * @param s String.
	 * @param pos Int position.
	 * @param c replacement String.
	 * @return modified String.
	 */
	private String replaceStringAt(String s, String c, int startIndex, int endIndex) {
		return s.substring(0,startIndex) + c + s.substring(endIndex);
	}
	
	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

}
