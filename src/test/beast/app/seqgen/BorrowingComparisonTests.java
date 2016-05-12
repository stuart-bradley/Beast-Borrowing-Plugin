package test.beast.app.seqgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import beast.app.seqgen.LanguageSequenceGen;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import java.io.*;

public class BorrowingComparisonTests {
	static final double TREERATE = 0.00055;
	static final int POPSIZE = 80;
	static final int[] BORROWRATES = {0,1,5,10,15,20,30,40,50};

	public static void main(String[] args) throws Exception {
		String fileLoc = args[0];
		File inputFile = new File(fileLoc);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputFile);
		doc.getDocumentElement().normalize();
		for (int rate : BORROWRATES) {
			System.out.println();
			Tree yuleNew = randomYuleTree(POPSIZE, TREERATE);
			Document constraints = generateConstraints(yuleNew, 2000.0);
			Document docNew = documentCopy(doc);
			
			String[] langSeqGenArgs = {"BorrowingComparisons/GTR_Borrow_" + rate + "_Input.xml","1","BorrowingComparisons/GTR_Borrow_" + rate + "_Output.xml"};
			LanguageSequenceGen.main(langSeqGenArgs);
			
			Document seqs = dBuilder.parse("BorrowingComparisons/GTR_Borrow_" + rate + "_Output.xml");
			Element dataElem = (Element) seqs.getElementsByTagName("data").item(0);
			dataElem.setAttribute("id", "GTR1");
			dataElem.setAttribute("name", "Alignment");
			NodeList sequences = seqs.getElementsByTagName("sequence");
			for (int i = 0; i < sequences.getLength(); i++) {
				Element s = (Element) sequences.item(i);
				s.setAttribute("totalcount", "2");
				s.setAttribute("id", "Sequence."+i);
			}
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

	private static Document generateConstraints (Tree tree, double minConstraintHeight) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.newDocument();
		List<Node> constraintNodes = getConstraintNodes(tree, minConstraintHeight);
		Element rootElem = doc.createElement("root");
		doc.appendChild(rootElem);
		for (int i = 0; i < constraintNodes.size(); i++) {
			Node n = constraintNodes.get(i);
			List<Node> children = n.getAllChildNodes();

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
