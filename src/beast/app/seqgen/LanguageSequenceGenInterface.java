package beast.app.seqgen;

import java.io.File;

import beast.app.util.Application;
import beast.core.Description;
import beast.core.Input;
import beast.core.Runnable;
import beast.core.Input.Validate;

/*
 * LanguageSequenceGenInterface class 
 * 
 * Entry point for Beauti BeastApp system. Can be used as
 * both a command line and a GUI interface.
 * 
 * @author Stuart Bradley (sbra886@aucklanduni.ac.nz)
 * @version 1.0
 * 
 * The command line can
 * be to close too the metal, 
 * a GUI is nice.
 */

@Description("The language sequence generation takes a tree and a root language, "
		+ "and returns leaf languages mutated according to the binary GTR or stochastic-Dollo models of evolution. "
		+ "There is additional support for meaning classes, and missing data.")
public class LanguageSequenceGenInterface extends Runnable {
	File file = null;
	public Input<beast.app.util.XMLFile> fileInput = new Input<>("input", "BEAST xml for sequence generation",
			Validate.REQUIRED);
	public Input<Integer> numberOfMeaningClassesInput = new Input<>("meaningClasses",
			"Number of meaning classes (default 1 [No classes])", 1, Validate.REQUIRED);
	public Input<beast.app.util.OutFile> fileOutput = new Input<>("output", "Name of output file");

	@Override
	public void initAndValidate() {
	}

	@Override
	public void run() throws Exception {
		String fileInputString = fileInput.get().getPath().replace("\\", "/");
		String meaningClasses = numberOfMeaningClassesInput.get().toString();
		String fileOutputString = fileOutput.get().getPath().replace("\\", "/");

		String[] args;

		if (fileOutputString.equals("") || fileOutputString.equals("[[none]]")) {
			args = new String[] { fileInputString, meaningClasses };
		} else {
			args = new String[] { fileInputString, meaningClasses, fileOutputString };
		}

		LanguageSequenceGen.main(args);
		
		// Check if default behavior is used, and delete erroneous file.
		File file = new File("[[none]]");
		if (file.exists()) {
			file.delete();
		}

	}

	public static void main(String[] args) throws Exception {
		new Application(new LanguageSequenceGenInterface(), "Language Sequence Generation", args);
	}

}
