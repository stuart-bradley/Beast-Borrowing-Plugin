package beastborrowingplugin.thesisanalysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
	
	protected void analyseHeights(String loc) {
		ArrayList<ArrayList<Double>> heightPercentageDifferences = new ArrayList<ArrayList<Double>>();
		for (int rate : BORROWRATES) {
			HashMap <String, AnalysisObject> rateObjects = analysisObjects.get(""+rate);
			ArrayList<Double> rateHeights = new ArrayList<Double>();
			rateHeights.add((double) rate);
			//iterating over values only
			for (AnalysisObject a : rateObjects.values()) {
				Double totalNumber = 0.0;
				Double totalDiff  = 0.0;
				Double startingTreeHeight = a.startingTreeHeight;
				for (Double treeHeight : a.heights) {
					totalNumber++;
					totalDiff += Math.abs(startingTreeHeight - treeHeight);
					
				}
				rateHeights.add((totalDiff/totalNumber) /startingTreeHeight);
			}
			heightPercentageDifferences.add(rateHeights);
		}
		listToCSV(heightPercentageDifferences, "BorrowingComparisons/Results/heights.csv");
	}

	private static <T> void listToCSV(ArrayList<T> l, String fileName) {
		final String NEW_LINE_SEPARATOR = "\n";
		final String NEW_COLUMN_SEPARATOR = ",";
		FileWriter fW = null;

		try {
			fW = new FileWriter(fileName);
			if (l.get(0) instanceof List<?>){
				int maxLen = 0;
				for (T i : l) {
					int size = ((List<?>) i).size();
					if (size > maxLen) {
						maxLen = size;
					}
				}
				for (int i = 0; i < maxLen; i++) {
					for (int j = 0; j < l.size(); j++) {
						List<?> col = (List<?>) l.get(j);
						try {
						fW.append(String.valueOf(col.get(i)));
						fW.append(NEW_COLUMN_SEPARATOR);
						} catch (Exception e) {}
					}
					fW.append(NEW_LINE_SEPARATOR);
				}
			} else {
				for (T i : l) {
					fW.append(String.valueOf(i));
					fW.append(NEW_LINE_SEPARATOR);
				}
			}
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			try {
				fW.flush();
				fW.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) { 
		BatchXMLAnalysis analysis = new BatchXMLAnalysis("BorrowingComparisons/BeastXMLs", "BorrowingComparisons/BeastXMLs","BorrowingComparisons");
		analysis.analyseHeights("BorrowingComparisons/Results");
	}
}
