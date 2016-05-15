package beastborrowingplugin.thesisanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class BatchXMLAnalysis {
	protected HashMap<String, File> logs = new HashMap<String, File>();
	protected HashMap<String, File> trees = new HashMap<String, File>();
	protected HashMap<String, File> inputs = new HashMap<String, File>();
	static final int[] BORROWRATES = {0,1,5,10,15,20,30,40,50};
	
	protected HashMap<String, HashMap<String, AnalysisObject>> analysisObjects = new HashMap<String, HashMap<String, AnalysisObject>>();

	public BatchXMLAnalysis(String logFileDir, String treeFileDir, String inputFileDir) {
		String[] dirs = {logFileDir, treeFileDir, inputFileDir}; 

		for (String dir : dirs) {
			File directory = new File(dir);
			File[] files = directory.listFiles();
			for (File f : files) {
				String name = f.getName();
				String ext = name.substring(name.lastIndexOf(".") + 1);
				if (ext.equals("log")) {
					logs.put(f.getName(),f);
				} else if (ext.equals("trees")) {
					trees.put(f.getName(),f);
				} else if (ext.equals("xml") && name.contains("Input")) {
					inputs.put(f.getName(),f);
				}
			}
		}
		
		for (int rate : BORROWRATES) {
			analysisObjects.put(""+rate, new HashMap<String, AnalysisObject>());
			for (int i = 0; i < 100; i++) {
				File log = logs.get("GTR_new_"+rate+"_"+i+".log");
				File tree = trees.get("GTR_new_"+rate+"_"+i+".trees");
				File input = inputs.get("GTR_Borrow_"+rate+"_"+i+"_Input.xml");
				if (log != null && tree != null && input != null) {
					analysisObjects.get(""+rate).put(""+i, new AnalysisObject(log, tree, input));
				}
			}
		}
	}
	
	protected void analyseHeights() {
		ArrayList<Double> heightPercentageDifferences = new ArrayList<Double>();
		for (int rate : BORROWRATES) {
			HashMap <String, AnalysisObject> rateObjects = analysisObjects.get(""+rate);
			//iterating over values only
			for (AnalysisObject a : rateObjects.values()) {
				Double totalNumber = 0.0;
				Double totalDiff  = 0.0;
				Double startingTreeHeight = a.startingTreeHeight;
				for (Double treeHeight : a.heights) {
					totalNumber++;
					totalDiff += Math.abs(startingTreeHeight - treeHeight);
					
				}
				heightPercentageDifferences.add((totalDiff/totalNumber) /startingTreeHeight);
			}	
		}
	}


	public static void main(String[] args) { 
		BatchXMLAnalysis analysis = new BatchXMLAnalysis("BorrowingComparisons/BeastXMLs", "BorrowingComparisons/BeastXMLs","BorrowingComparisons");
		analysis.analyseHeights();
	}
}
