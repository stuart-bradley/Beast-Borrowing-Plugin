package beast.evolution.missingdatamodel;

import java.util.ArrayList;

import beast.core.CalculationNode;
import beast.core.Description;
import beast.evolution.alignment.Sequence;
import beast.util.Randomizer;

/*
 * MissingDataModel Abstract Class
 * 
 * This class is a basis for other Missing Data Models (MDM), it contains both abstract methods 
 * required for any MDM, as well as various helper methods.
 * 
 * @author Stuart Bradley (sbra886@aucklanduni.ac.nz)
 * @version 1.0
 * 
 */
@Description("Abstract class for generating missing data")
public abstract class MissingDataModel extends CalculationNode {

	/*
	 * ABSTRACT METHODS
	 */

	/*
	 * BEAST Object required class.
	 * 
	 * @see
	 * beast.evolution.substitutionmodel.SubstitutionModel.Base#initAndValidate(
	 * )
	 */
	public abstract void initAndValidate();

	public abstract ArrayList<Sequence> generateMissingData(ArrayList<Sequence> a, String meaningClasses) throws Exception;

	protected int binomalDraw(int n, double p) {
		int x = 0;
		for(int i = 0; i < n; i++) {
			if(Randomizer.nextDouble() < p) {
				x++;
			}
		}
		return x;
	}


	}
