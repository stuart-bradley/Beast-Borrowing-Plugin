package beastborrowingplugin.thesisanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchXMLAnalysis {
	protected HashMap<String, File> logs = new HashMap<String, File>();
	protected HashMap<String, File> trees = new HashMap<String, File>();
	protected HashMap<String, File> inputs = new HashMap<String, File>();
	protected ArrayList<ArrayList<Double>> heightPercentageDifferences = new ArrayList<ArrayList<Double>>();
	protected ArrayList<ArrayList<Double>> topologyDifferences = new ArrayList<ArrayList<Double>>();
	static final int[] BORROWRATES = {0,1,5,10,15,20,30,40,50};

	protected HashMap<String, AnalysisObject> analysisObjects = new HashMap<String, AnalysisObject>();

	public BatchXMLAnalysis(String logFileDir, String treeFileDir, String inputFileDir) {
		String[] dirs = {logFileDir, treeFileDir, inputFileDir}; 
		System.out.println("Reading in files.");
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

		System.out.println("Creating Analysis Objects.");
		for (int rate : BORROWRATES) {
			analysisObjects = new HashMap<String,AnalysisObject>();
			for (int i = 0; i < 100; i++) {
				File log = logs.get("GTR_new_"+rate+"_"+i+".log");
				File tree = trees.get("GTR_new_"+rate+"_"+i+".trees");
				File input = inputs.get("GTR_Borrow_"+rate+"_"+i+"_Input.xml");
				if (log != null && tree != null && input != null) {
					System.out.println("Create:" + rate +"_"+i);
					analysisObjects.put(""+i, new AnalysisObject(log, tree, input));
				}
			}
			analyseTopology("C:/Users/Stuart/Downloads/BorrowingComparisons/Results",rate);
			analyseHeights("C:/Users/Stuart/Downloads/BorrowingComparisons/Results",rate);
		}

		listToCSV(heightPercentageDifferences, "C:/Users/Stuart/Dropbox/Results/heights.csv");
		listToCSV(topologyDifferences, "C:/Users/Stuart/Dropbox/Results/quartet.csv");
	}

	protected void analyseHeights(String loc, int rate) {
		ArrayList<Double> rateHeights = new ArrayList<Double>();
		rateHeights.add((double) rate);
		//iterating over values only
		for (AnalysisObject a : analysisObjects.values()) {
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

	protected void analyseTopology(String loc, int rate) {
		File resFolder = new File(loc+"/Tmp");
		File qtDist = new File("qDist/quartet_dist.exe");
		resFolder.mkdir();
		System.out.println("Analysing rate: " + rate);
		ArrayList<Double> rateTopologies = new ArrayList<Double>();
		rateTopologies.add((double) rate);

		int num = 1;
		int totalNum = analysisObjects.keySet().size();
		for (Map.Entry<String, AnalysisObject> entry : analysisObjects.entrySet()) {
			System.out.println("Analysing object: " + num +"/"+totalNum);
			num++;
			File startPath = new File(resFolder.getPath()+"/startTree_"+entry.getKey()+".tree");
			createTreeFile(startPath.getPath(), entry.getValue().startingTree);
			List<String> resTrees = entry.getValue().trees;
			Collections.shuffle(resTrees);
			Double totalDiff = 0.0;
			for (int i = 0; i < 300; i++) {
				try {
					String t = resTrees.get(i);
					File treePath = new File(resFolder.getPath()+"/tree_"+entry.getKey()+"_"+i+".tree");
					createTreeFile(treePath.getPath(), t);
					ProcessBuilder builder = new ProcessBuilder(
							"cmd.exe", "/c", qtDist.getAbsolutePath(), "-v", "\""+startPath.getAbsolutePath()+"\"", "\""+treePath.getAbsolutePath()+"\"");
					builder.redirectErrorStream(true);
					Process p = builder.start();
					BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = r.readLine()) != null) {
						totalDiff  += Double.parseDouble(line.split("\t")[3]);
					}
				} catch (Exception e) {}
			}
			rateTopologies.add(totalDiff/resTrees.size());
		}
		topologyDifferences.add(rateTopologies);

		try {
			System.out.println();
			delete(resFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createTreeFile(String fileName, String tree) {
		FileWriter fW = null;

		try {
			fW = new FileWriter(fileName);
			fW.append(tree);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fW.flush();
				fW.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void delete(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		if (!f.delete())
			throw new FileNotFoundException("Failed to delete file: " + f);
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
			e.printStackTrace();
		} finally {
			try {
				fW.flush();
				fW.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) { 
		BatchXMLAnalysis analysis = new BatchXMLAnalysis("C:/Users/Stuart/Downloads/BorrowingComparisons/BeastXMLs", "C:/Users/Stuart/Downloads/BorrowingComparisons/BeastXMLs","C:/Users/Stuart/Downloads/BorrowingComparisons");
	}
}