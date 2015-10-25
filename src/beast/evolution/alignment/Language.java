package beast.evolution.alignment;

import java.util.ArrayList;
import java.util.TreeMap;
import beast.core.Description;
import beast.core.Input;

/*
 * Language Object 
 * 
 * The Language class is used in the Language Generation Plugin, 
 * it holds basic information on a single language. 
 * 
 * @author Stuart Bradley (sbra886@aucklanduni.ac.nz)
 * @version 1.0
 * @see beast.core.BEASTObject
 * 
 * Language generation
 * coding has begun. BEAST has
 * a brand new purpose.
 */
@Description("Single Language for the Language Package")
public class Language extends Sequence {
	
	/** Internal representation of the language */
	protected ArrayList<Integer> language = new ArrayList<Integer>();
	/** List of mutations on language, excludes initial state */ 
	protected TreeMap<Double, ArrayList<Integer>> mutations = new TreeMap<Double, ArrayList<Integer>>();
	
	public Language() throws Exception {
	}
	
	public Language(ArrayList<Integer> lang) {
        this.language = lang;
    }
	
	/*
	 * Converts String input to language ArrayList<Integer>.
	 * @see beast.core.BEASTInterface#initAndValidate()
	 * @exception Any exception.
	 */
	@Override
	public void initAndValidate() throws Exception {
		String[] tokens = dataInput.get().split("");
		for (int i = 0; i < tokens.length; i++) {
			language.add(Integer.parseInt(tokens[i]));
		}
	}	
	
	/*
	 * @return traits An Int that counts the number of 1's in language.
	 */
	public int getBirths() {
        int traits = 0;
        for (int trait : this.language) {
            if (trait == 1) {
                traits += 1;
            }
        }
        return traits;
    }
	
	/*
	 * Adds a single mutation to mutation TreeMap
	 * @param t Time of mutation event.
	 * @param i ArrayList<Integer> of new language.
	 */
	
	public void addMutation(Double t, ArrayList<Integer> i) {
		mutations.put(t, i);
	}
	
	/*
	 * Auto-Generated Getters/Setters
	 */
	
	public TreeMap<Double, ArrayList<Integer>> getMutations() {
		return mutations;
	}

	public void setMutations(TreeMap<Double, ArrayList<Integer>> mutations) {
		this.mutations = mutations;
	}

	public void setLanguage(ArrayList<Integer> language) {
		this.language = language;
	}
	
	public ArrayList<Integer> getLanguage() {
        return language;
    }
}
