package beast.evolution.missingdatamodel;

import java.util.ArrayList;
import java.util.Collections;

import beast.core.Input;
import beast.evolution.alignment.Sequence;
import beast.evolution.substitutionmodel.LanguageSubsitutionModel;

/*
 * MissingMeaningClassModel Class
 * 
 * Determines which meaning classes appear unknown in the final alignment.
 * Does this by virtue of a simple coin-flip for each meaning class.
 * 
 * @author Stuart Bradley (sbra886@aucklanduni.ac.nz)
 * @version 1.0
 * 
 * Do words really have 
 * any meaning when they don't have
 * their meaning classes?
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
	/*
	 * (non-Javadoc)
	 * @see beast.evolution.missingdatamodel.MissingDataModel#generateMissingData(java.util.ArrayList, java.lang.String)
	 * 
	 * Generates missing data on a per meaning class basis.
	 */
	@Override
	public ArrayList<Sequence> generateMissingData(ArrayList<Sequence> a, String meaningClasses) throws Exception {
		String[] mcArray = meaningClasses.split(" ");
		ArrayList<Integer> meaningClassesList = new ArrayList<Integer>();
		for (String s : mcArray) {
			meaningClassesList.add(Integer.parseInt(s));
		}
		for (int mc : meaningClassesList) {
			a = meaningClassToUnknown(a, mc, meaningClassesList);
		}
		return a;
	}

	/*
	 * Randomly sets cognates as missing according to a binomial distribution.
	 * 
	 * @param a, List of sequences
	 * 
	 * @param startIndex, start position of meaning class
	 * 
	 * @param mcList, all meaning classes (to find end position)
	 * 
	 * @return Sequence with missing data
	 */
	private ArrayList<Sequence> meaningClassToUnknown(ArrayList<Sequence> a, int startIndex, ArrayList<Integer> mcList)
			throws Exception {
		int mcIndex = mcList.indexOf(startIndex);
		// MC is last in list.
		int endIndex = a.get(0).getData().length();
		if (mcIndex != mcList.size() - 1) {
			endIndex = mcList.get(mcIndex + 1);
		}

		int totalSeqs = a.size();
		int totalCogsInMeaningClass = (endIndex - startIndex) * totalSeqs;
		int missingEvents = binomalDraw(totalCogsInMeaningClass, rate);
		ArrayList<int[]> positions = new ArrayList<int[]>();
		for (int i = 0; i < totalSeqs; i++) {
			for (int j = startIndex; j < endIndex; j++) {
				int[] pos = {i,j};
				positions.add(pos);
			}
		}
		Collections.shuffle(positions);
		for (int i = 0; i < missingEvents; i++) {
			int[] pos = positions.get(i);
			Sequence s = a.get(pos[0]);
			String taxa = s.getTaxon();
			String newData = LanguageSubsitutionModel.replaceCharAt(s.getData(),pos[1], "?");
			a.set(pos[0], new Sequence(taxa, newData));
		}

		return a;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

}
