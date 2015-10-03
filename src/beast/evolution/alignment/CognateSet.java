package beast.evolution.alignment;

import java.util.ArrayList;

import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;

/*
 * CognateSet Object 
 * 
 * The CognateSet Class houses the "alignment" of multiple languages, without requiring the larger Alignment structure.
 * 
 * @author Stuart Bradley (sbra886@aucklanduni.ac.nz)
 * @version 1.0
 * 
 * I hope extending
 * nothing doesn't screw anything
 * up, fingers crossed. 
 */
@Description("Data Structure for multiple Languages")
public class CognateSet extends Alignment {
	public Input<Language> rootInput = new Input<Language>("root", "inital language", Validate.REQUIRED);
	/** List of languages */ 
    protected ArrayList<Language> languageList;
    /** Length of languages, only used in SD model. */
    protected int stolloLength;
    
    /*
     * Constructor
     * 
     * @param root initial language
     */
    public CognateSet(Language root) {
        this.languageList = new ArrayList<Language>();
        this.languageList.add(root);

        this.stolloLength = root.getLanguage().size();
    }
    
    @Override
    public void initAndValidate() throws Exception {
    	this.languageList = new ArrayList<Language>();
        this.languageList.add(rootInput.get());

        this.stolloLength = rootInput.get().getLanguage().size();
    }
    
    public Language getLanguage(int index) {
    	return this.languageList.get(index);
    }
    
    /*
	 * Auto-Generated Getters/Setters
	 */
    
    public ArrayList<Language> getLanguageList() {
		return languageList;
	}

	public void setLanguageList(ArrayList<Language> languageList) {
		this.languageList = languageList;
	}

	public int getStolloLength() {
    	return this.stolloLength;
    }
    
    public void setStolloLength(int i) {
    	this.stolloLength = i;
    }
}
