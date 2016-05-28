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
	@Override
	public ArrayList<Sequence> generateMissingData(ArrayList<Sequence> a, String meaningClasses) throws Exception {
		// Last Sequence is Meaning Classes (ignored in this model).
		for (int i = 0; i < a.size(); i++) {
			if (Randomizer.nextDouble() <= rate) {
				a.set(i, languageToUnknown(a.get(i)));
			}
		}
		return a;
	}

	private Sequence languageToUnknown(Sequence s) throws Exception {
		String taxa = s.getTaxon();
		String seq = s.getData();
		String unknown = "";
		for (int i = 0; i < seq.length(); i++) {
			unknown += "?";
		}
		return new Sequence(taxa, unknown);
	}
	*/
	
	@Override
	public ArrayList<Sequence> generateMissingData(ArrayList<Sequence> a, String meaningClasses) throws Exception {
		for (int i = 0; i < a.size(); i++) {
			
			a.set(i, languageToUnknown(a.get(i)));
		}
		return a;
	}

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
