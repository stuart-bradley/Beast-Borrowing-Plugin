package beast.evolution.missingdatamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import beast.core.Input;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Sequence;
import beast.util.Randomizer;

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
	
	@Override
	public ArrayList<Sequence> generateMissingData(ArrayList<Sequence> a) throws Exception {
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
	
	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

}
