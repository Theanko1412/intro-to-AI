package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Solution {

	public static void main(String ... args) {

		if(args.length < 2) {
			System.err.println("Im expecting <training_file> <test_file> and optional <depth> provided as arguments");
			System.exit(1);
		}

		//take input args
		List<String> arguments = Arrays.stream(args).map(String::toLowerCase).map(String::strip).toList();
		String trainingFile = arguments.get(0);
		String testFile = arguments.get(1);
		int maxDepth = arguments.size() == 3 ? Integer.parseInt(arguments.get(2)) : Integer.MAX_VALUE;

		//parse csv files
		List<List<String>> trainingData = parseCSV(trainingFile);
		List<List<String>> testData = parseCSV(testFile);

		List<String> trainingHeader = new ArrayList<>(trainingData.get(0));
		trainingHeader.remove(trainingHeader.size() - 1);

		trainingData.remove(0);
		List<String> testHeader = testData.get(0);
		testData.remove(0);

//		System.out.println("Training header:" + trainingHeader);
//		System.out.println("Training data:" + trainingData);
//		System.out.println("Test header:" + testHeader);
//		System.out.println("Test data:" + testData);

		Locale.setDefault(Locale.US);

		//train model
		ID3 model = new ID3();
    System.out.println(model.fit(trainingData, trainingData, trainingHeader, maxDepth));

		//print model
    System.out.println("[BRANCHES]:");
		printTree(model, model.getTree(), "", 1);

		//predict based on built model
    System.out.print("[PREDICTIONS]: ");
		List<String> predictions = model.predict(testData);
		for(String prediction : predictions) {
			System.out.printf("%s ", prediction);
		}

		//print accuracy
		System.out.printf("%n[ACCURACY]: %s%n", String.format("%.5f", model.accuracy(testData, predictions)));

		//print confusion matrix
		System.out.println("[CONFUSION_MATRIX]:");
		System.out.println(model.confusionMatrix(testData, predictions));
	}


	public static void printTree(ID3 model, TreeNode node, String prefix, int depth) {
		//handle depth 0 print
		if(model.isDepthZero()) {
			if(node instanceof Leaf leaf) {
				System.out.println(leaf.getValue());
			}
			return;
		}

		//similar logic as predict, looking what type current element is and then printing it
		//keeping depth and prefix(prev output) for recursion
		if (node instanceof Leaf) {
			Leaf leaf = (Leaf) node;
			System.out.println(prefix + leaf.getName());
		} else if (node instanceof Node currentNode) {
			List<TreeNode> children = currentNode.getChildren();
			for (TreeNode child : children) {
				String newPrefix;
				if(child instanceof Leaf leaf) {
					newPrefix = prefix + depth + ":" + currentNode.getValue() + "=" + leaf.getName() + " ";
					System.out.println(newPrefix + leaf.getValue());
				} else {
					Node childNode = (Node) child;
					newPrefix = prefix + depth + ":" + currentNode.getValue() + "=" + childNode.getParentValue() + " ";
					printTree(model, childNode, newPrefix, depth + 1);
				}
			}
		}
	}

	private static List<List<String>> parseCSV(String csvfile) {
		List<List<String>> data = new ArrayList<>();

		//regular parsing by ","
		try (Scanner scanner = new Scanner(new File(csvfile))) {
			while (scanner.hasNextLine()) {
				data.add(Arrays.stream(scanner.nextLine().split(","))
						.map(String::strip)
						.toList());
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + csvfile);
		}
		return data;
	}

}
