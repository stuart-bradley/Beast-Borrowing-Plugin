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

public class BatchXMLAnalysis {
	protected HashMap<String, File> logs = new HashMap<String, File>();
	protected HashMap<String, File> trees = new HashMap<String, File>();
	protected HashMap<String, File> inputs = new HashMap<String, File>();
	protected ArrayList<ArrayList<Double>> heightPercentageDifferences = new ArrayList<ArrayList<Double>>();
	protected ArrayList<ArrayList<Double>> topologyDifferences = new ArrayList<ArrayList<Double>>();
	static final int[] BORROWRATES = {0,1,5,10,15,20,30,40,50};

	protected HashMap<String, AnalysisObject> analysisObjects = new HashMap<String, AnalysisObject>();

	public BatchXMLAnalysis(String logFileDir, String treeFileDir, String inputFileDir, String prefix) {
		System.out.println("Calculating: " + prefix);
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
		for (int rate : BORROWRATES) {
			System.out.println("Calculating Rate: " + rate);
			System.out.println();
			ArrayList<Double> rateHeights = new ArrayList<Double>();
			rateHeights.add((double) rate);
			ArrayList<Double> rateTopologies = new ArrayList<Double>();
			rateTopologies.add((double) rate);
			
			File resFolder = new File(inputFileDir+"/Results"+"/Tmp");
			File qtDist = new File("qDist/quartet_dist.exe");
			resFolder.mkdir();
			
			for (int i = 1; i < 101; i++) {
				System.out.println("Calculating Node: " + i);
				File log = logs.get(prefix+"_new_"+rate+"_"+i+".log");
				File tree = trees.get(prefix+"_new_"+rate+"_"+i+".trees");
				File input = inputs.get(prefix+"_Borrow_"+rate+"_"+i+"_Input.xml");
				if (log != null && tree != null && input != null) {
					AnalysisObject a = new AnalysisObject(log, tree, input);
					rateHeights.add(analyseHeight(a));
					rateTopologies.add(analyseTopology(a, i, resFolder, qtDist));
				}
			}
			topologyDifferences.add(rateTopologies);
			heightPercentageDifferences.add(rateHeights);
			try {
				delete(resFolder);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		listToCSV(heightPercentageDifferences, inputFileDir+"/heights_"+prefix+".csv");
		listToCSV(topologyDifferences, inputFileDir+"/quartet_"+prefix+".csv");
	}


	protected Double analyseHeight(AnalysisObject a) {
		Double totalDiff  = 0.0;
		Double startingTreeHeight = a.startingTreeHeight;
		List<Double> heights = a.heights;
		for (int i = 0; i < heights.size(); i++) {
			Double treeHeight = heights.get(i);
			totalDiff += startingTreeHeight - treeHeight;
		}
		return ((totalDiff/heights.size()) /startingTreeHeight);
	}

	protected Double analyseTopology(AnalysisObject a, int num, File resFolder, File qtDist) {
		File startPath = new File(resFolder.getPath()+"/startTree_"+num+".tree");
		createTreeFile(startPath.getPath(), a.startingTree);
		List<String> resTrees = a.trees;
		Double totalDiff = 0.0;
		for (int i = 0; i < resTrees.size(); i++) {
			try {
				String t = resTrees.get(i);
				File treePath = new File(resFolder.getPath()+"/tree_"+num+"_"+i+".tree");
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return (totalDiff/resTrees.size());
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
		//BatchXMLAnalysis analysis = new BatchXMLAnalysis("F:/Downloads/COV/BeastXMLs", "F:/Downloads/COV/BeastXMLs","F:/Downloads/COV", "COV");
		BatchXMLAnalysis analysis = new BatchXMLAnalysis(args[0], args[1],args[2], args[3]);
		//BatchXMLAnalysis analysis1 = new BatchXMLAnalysis("F:/Downloads/SD/BeastXMLs", "F:/Downloads/SD/BeastXMLs","F:/Downloads/SD", "SD");
		//BatchXMLAnalysis analysis2 = new BatchXMLAnalysis("F:/Downloads/GTR/BeastXMLs", "F:/Downloads/GTR/BeastXMLs","F:/Downloads/GTR", "GTR");
	}
}
