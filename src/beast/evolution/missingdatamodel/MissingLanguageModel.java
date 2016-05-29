package beast.evolution.missingdatamodel;

import java.util.ArrayList;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.alignment.Sequence;
import beast.evolution.substitutionmodel.LanguageSubsitutionModel;
import beast.util.Randomizer;

/*
 * MissingLanguageModel Class
 * 
 * Determines which languages appear unknown in the final alignment.
 * Does this by virtue of a simple coin-flip for each language.
 * 
 * @author Stuart Bradley (sbra886@aucklanduni.ac.nz)
 * @version 1.0
 * 
 * This class was inspired
 * by the Hunns and their lack of
 * vocabulary
 */
@Description("Class for sythesis of missing languages")
public class MissingLanguageModel extends MissingDataModel {
	public Input<Double> rateInput = new Input<Double>("rate", "missing language rate");

	/** rate */
	protected double rate;

	public MissingLanguageModel() throws Exception {
	}

	public MissingLanguageModel(double r) {
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
	 * Generates missing data on a per language basis.
	 */
	@Override
	public ArrayList<Sequence> generateMissingData(ArrayList<Sequence> a, String meaningClasses) throws Exception {
		for (int i = 0; i < a.size(); i++) {
			a.set(i, languageToUnknown(a.get(i)));
		}
		return a;
	}

	/*
	 * Randomly sets cognates as missing according to a binomial distribution.
	 * 
	 * @param s, sequence
	 * 
	 * @return Sequence with missing data
	 */
	private Sequence languageToUnknown(Sequence s) throws Exception {
		String taxa = s.getTaxon();
		String seq = s.getData();
		int missingEvents = binomalDraw(seq.length(), rate);
		int[] randPos = Randomizer.shuffled(seq.length());
		for (int i = 0; i < missingEvents; i++) {
			seq = LanguageSubsitutionModel.replaceCharAt(seq, randPos[i], "?");
		}
		return new Sequence(taxa, seq);
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

}
