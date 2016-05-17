package beastborrowingplugin.thesisanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import beast.app.beastapp.BeastMain;
import beast.app.seqgen.LanguageSequenceGen;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;

public class BorrowingComparisonTests {
	static final double TREERATE = 0.00055;
	static final int POPSIZE = 80;
	static final int[] BORROWRATES = {0,1,5,10,15,20,30,40,50};

	public static void main(String[] args) throws Exception {
		String fileLoc = args[0];
		String rate = args[3];
		File inputFile = new File(fileLoc);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputFile);
		doc.getDocumentElement().normalize();

		Tree yuleNew = randomYuleTree(POPSIZE, TREERATE);
		Document constraints = generateConstraints(yuleNew, 2500.0);
		Document docNew = documentCopy(doc);
		Document rateInputFileNew = documentCopy(dBuilder.parse("BorrowingComparisons/"+args[1]+"/"+args[1]+"_Borrow_" + rate +"_Input.xml"));

		// Change Yule tree. 
		Element treeOld = (Element) rateInputFileNew.getElementsByTagName("tree").item(0);
		treeOld.setAttribute("newick", yuleNew.getRoot().toNewick());
		writeXML(rateInputFileNew, "BorrowingComparisons/"+args[1]+"/"+args[1]+"_Borrow_" + rate + "_"+args[2]+"_Input.xml");

		// Run LangSeqGen.
		String[] langSeqGenArgs = {"BorrowingComparisons/"+args[1]+"/"+args[1]+"_Borrow_" + rate + "_"+args[2]+"_Input.xml","1","BorrowingComparisons/"+args[1]+"_Borrow_" + rate + "_"+args[2]+"_Output.xml"};
		LanguageSequenceGen.main(langSeqGenArgs);

		// Replace constraints.
		NodeList dataElemOldList = docNew.getElementsByTagName("distribution");
		NodeList dataElemNewList = constraints.getElementsByTagName("distribution");
		for (int i = 0; i < dataElemOldList.getLength(); i++) {
			Element dist = (Element) dataElemOldList.item(i);
			String distID = dist.getAttribute("id");
			if (distID.endsWith(".prior")) {
				for (int j = 0; j < dataElemNewList.getLength(); j++) {
					Element distNew = (Element) dataElemNewList.item(j);
					String distIDNew = distNew.getAttribute("id");
					if (distID.equals(distIDNew)) {
						// Replace old data.
						org.w3c.dom.Node importedNode = docNew.importNode(distNew, true);
						dist.getParentNode().replaceChild(importedNode, dist);
					}
				}
			}
		}

		// Get new data.
		Document seqs = dBuilder.parse("BorrowingComparisons/"+args[1]+"/"+args[1]+"_Borrow_" + rate + "_"+args[2]+"_Output.xml");
		Element dataElem = (Element) seqs.getElementsByTagName("data").item(0);
		dataElem.setAttribute("id", "GTR1");
		dataElem.setAttribute("name", "alignment");
		NodeList sequences = seqs.getElementsByTagName("sequence");
		for (int i = 0; i < sequences.getLength(); i++) {
			Element s = (Element) sequences.item(i);
			s.setAttribute("totalcount", "2");
			s.setAttribute("id", "Sequence.0"+i);
		}

		// Replace old data.
		Element dataElemOld = (Element) docNew.getElementsByTagName("data").item(0);
		Element dataElemNew = (Element) seqs.getElementsByTagName("data").item(0);
		org.w3c.dom.Node importedNode = docNew.importNode(dataElemNew, true);
		dataElemOld.getParentNode().replaceChild(importedNode, dataElemOld);

		//Edit logging files.
		dataElemOldList = docNew.getElementsByTagName("logger");
		for (int i = 0; i < dataElemOldList.getLength(); i++) {
			Element logger = (Element) dataElemOldList.item(i);
			if (logger.hasAttribute("fileName")) {
				String loggerFileName = logger.getAttribute("fileName");
				String loggerFileExtension = loggerFileName.replaceAll(".*\\.", "");
				logger.setAttribute("fileName", args[1]+"_new_" + rate + "_"+args[2]+"."+loggerFileExtension);
			}
		}


		// Write to XML file.
		writeXML(docNew, "BorrowingComparisons/"+args[1]+"/BeastXMLs/"+args[1]+"_new_" + rate + "_"+args[2]+".xml");

		// BEAST Run.
		String[] beastArgs = {"-overwrite", "-working","BorrowingComparisons/"+args[1]+"/BeastXMLs/"+args[1]+"_new_" + rate + "_"+args[2]+".xml"};
		while (true) {
			try {
				BeastMain.main(beastArgs);
				break;
			} catch (Exception e) {}
		}
	}


	private static Document documentCopy (Document doc) throws Exception {
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer tx = tfactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		DOMResult result = new DOMResult();
		tx.transform(source,result);
		return (Document)result.getNode();
	}

	private static void writeXML (Document doc, String loc) throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		//StreamResult result = new StreamResult(System.out);
		StreamResult result = new StreamResult(new File(loc));
		transformer.transform(source, result);
	}

	private static Document generateConstraints (Tree tree, double minConstraintHeight) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.newDocument();
		List<Node> constraintNodes = getConstraintNodes(tree, minConstraintHeight);
		Element rootElem = doc.createElement("root");
		doc.appendChild(rootElem);
		for (int i = 0; i < constraintNodes.size(); i++) {
			Node n = constraintNodes.get(i);
			List<Node> children = n.getAllLeafNodes();

			Element distElem = doc.createElement("distribution");
			distElem.setAttribute("id", "Constraint_"+(i+1)+".prior");
			distElem.setAttribute("spec", "beast.math.distributions.MRCAPrior");
			distElem.setAttribute("tree", "@Tree.t:GTR1");
			rootElem.appendChild(distElem);

			Element taxonSetElem = doc.createElement("taxonset");
			taxonSetElem.setAttribute("id", "Constraint_"+(i+1));
			taxonSetElem.setAttribute("spec", "TaxonSet");
			distElem.appendChild(taxonSetElem);

			for (Node c : children) {
				Element taxonElem = doc.createElement("taxon");
				taxonElem.setAttribute("id", c.getID());
				taxonElem.setAttribute("spec", "Taxon");
				taxonSetElem.appendChild(taxonElem);
			}

			Element uniformElem = doc.createElement("Uniform");
			uniformElem.setAttribute("id", "Uniform.0"+(i+1));
			uniformElem.setAttribute("name", "distr");
			double height = n.getHeight();
			String upper = Math.ceil(height * 1.1) + "";
			String lower = Math.floor(height * 0.9) + "";
			uniformElem.setAttribute("lower", lower);
			uniformElem.setAttribute("upper", upper);
			distElem.appendChild(uniformElem);
		}
		return doc;
	}

	private static List<Node> getConstraintNodes (Tree tree, double minConstraintHeight) {
		List<Node> constraintNodes = new ArrayList<Node>();
		List<Node> possibleConstraintNodes = new ArrayList<Node>();
		for (Node n : tree.getNodesAsArray()) {
			if (n.getHeight() < minConstraintHeight) {
				continue;
			}
			List<Node> c2Children = n.getAllChildNodes();
			for (Node c1 : possibleConstraintNodes) {
				List<Node> c1Children = c1.getAllChildNodes();
				if (Collections.disjoint(c1Children, c2Children)) {
					constraintNodes.add(n);
					constraintNodes.add(c1);
					break;
				}
			}
			if (!constraintNodes.isEmpty()) {
				break;
			}
			possibleConstraintNodes.add(n);
		}
		return constraintNodes;
	}

	private static Tree randomYuleTree (int nodes, double l) throws Exception {
		Tree tr = new Tree();
		ArrayList<Node> nodeList = new ArrayList<Node>();
		double t = 0.0;
		int label = 1;
		for (int i = 0; i < nodes; i++) {
			Node n = new Node();
			n.setHeight(t);
			n.setNr(label);
			label++;
			nodeList.add(n);
		}

		while (nodeList.size() > 1) {
			t += Randomizer.nextExponential(nodeList.size()*l);

			int p_1_index = Randomizer.nextInt(nodeList.size());
			Node p1 = nodeList.remove(p_1_index);
			int p_2_index = Randomizer.nextInt(nodeList.size());
			Node p2 = nodeList.remove(p_2_index);

			Node parent = new Node();
			parent.setHeight(t);

			parent.setNr(label);
			label++;
			p1.setParent(parent);
			parent.addChild(p1);
			p2.setParent(parent);
			parent.addChild(p2);

			nodeList.add(parent);
		}
		tr.setRoot(nodeList.get(0));
		return new Tree(tr.toString());

	}

}
