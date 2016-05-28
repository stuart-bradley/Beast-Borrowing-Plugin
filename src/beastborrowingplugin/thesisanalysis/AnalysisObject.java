package beastborrowingplugin.thesisanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import beast.evolution.tree.Tree;

public class AnalysisObject {
	protected List<String> trees = new ArrayList<String>();
	protected String startingTree;
	protected List<Double> heights;
	protected Double startingTreeHeight;

	public AnalysisObject(File log, File tree, File input) {
		trees = processTreeFile(tree);
		Collections.shuffle(trees);
		trees = trees.subList(0, 300);
		startingTree = getStartingTree(input);
		heights = processLogFile(log);
		Collections.shuffle(heights);
		heights = heights.subList(0, 300);
		startingTreeHeight = getStartingTreeHeight();
	}

	private Double getStartingTreeHeight() {
		Tree startingTreeBeast;
		try {
			startingTreeBeast = new Tree(startingTree);
			return startingTreeBeast.getRoot().getHeight();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.0;
	}

	private List<Double> processLogFile(File log) {
		ArrayList<Double> heights = new ArrayList<Double>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\t";
		
		try {

			br = new BufferedReader(new FileReader(log));
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#") || line.startsWith("Sample")) {
					continue;
				} 
				String[] columns = line.split(cvsSplitBy);
				heights.add(Double.parseDouble(columns[5]));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return heights;
	}

	private String getStartingTree(File input) {
		String inputTree = "";
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(input);
			doc.getDocumentElement().normalize();
			Element treeOld = (Element) doc.getElementsByTagName("tree").item(0);
			inputTree = treeOld.getAttribute("newick")+";";
		} catch (Exception e) {

		}
		return inputTree;
	}

	private List<String> processTreeFile(File treeFile) {
		ArrayList<String> newickTrees = new ArrayList<String>();
		HashMap<String, String> conv = new HashMap<String, String>();

		try (BufferedReader br = new BufferedReader(new FileReader(treeFile))) {
			String line;
			boolean atTrees = false;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.contains("Begin trees")) {
					atTrees = true;
					continue;
				}
				if (! atTrees) {
					continue;
				}
				if (line.contains("Translate") || line.equals(";") || line.contains("End;")) {
					continue;
				} else if (line.startsWith("tree")) {
					newickTrees.add(convertTree(line, conv));
				} else {
					line = line.replace(",", "").trim();
					String[] numbers = line.split(" ");
					conv.put("("+numbers[0]+":", "("+numbers[1]+":");
					conv.put(","+numbers[0]+":", ","+numbers[1]+":");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return newickTrees;
	}

	private String convertTree (String treeOld, HashMap<String, String> conv) {
		treeOld = treeOld.replaceAll("\\[(.*?)\\]", "");
		Pattern pattern = Pattern.compile("\\(\\d+:|,\\d+:");
		Matcher matcher = pattern.matcher(treeOld);
		int offset = 0;
		while (matcher.find()) {
			String replace = conv.get(matcher.group());
			String head = (String) treeOld.subSequence(0, matcher.start() + offset);
			String tail = (String) treeOld.subSequence(matcher.end() + offset, treeOld.length());

			treeOld = head + replace + tail;

			if (matcher.group().length() > replace.length()) {
				offset --;
			} else if (matcher.group().length() < replace.length()) {
				offset ++;
			}
		}	    
		return treeOld.substring(treeOld.indexOf("=")+1);
	}
}