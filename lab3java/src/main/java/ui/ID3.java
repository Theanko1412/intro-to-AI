package ui;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ID3 {

  TreeNode tree;
  List<String> initialHeader;
  boolean depthZero = false;

  public ID3() {
  }

  // based on pseudocode from ppt
  public TreeNode fit(List<List<String>> D, List<List<String>> Dparent, List<String> X, int depth) {
    // checking if first call, setting initial header so i keep its value through recursion
    // also checking if depth is 0, if so, the root node is a leaf node
    if (initialHeader == null) {
      initialHeader = X;
      if (depth == 0) {
        depthZero = true;
      }
    }

    // if our set is empty we make a leaf with most frequent from parent
    // if we reach depth 0 but set is still not empety we look for most frequent in current set
    if (D.isEmpty() || depth == 0) {
      String v = argmaxv(D.isEmpty() ? Dparent : D);
      // if maxdepth is 0 root node equals leaf
      if (depthZero) {
        setTree(new Leaf(v));
      }

      return new Leaf(v);
    }

    // if we didnt reach the end we look for most frequent in current set
    String v = argmaxv(D);
    // if our header is empty or all our values in D are the same we just return the leaf with most
    // frequent
    if (X.isEmpty() || D.stream().map(row -> row.get(row.size() - 1)).collect(Collectors.toSet()).size() == 1) {
      return new Leaf(v);
    }

    // looking for most informative attribute
    String x = argmaxx(D, X);

    // getting all possible values in column x
    Set<String> values = filterByColumn(D, X, x);

    List<TreeNode> subtrees = new ArrayList<>();

    // for each value in column x recursively call fit
    for (String val : values) {
      int indexToCheck = X.indexOf(x);
      TreeNode t = fit(D.stream()
              // taking only rows where column x equals val
              .filter(row -> row.get(indexToCheck).equals(val))
              // removing the column x, all value will the the same
              // https://stackoverflow.com/questions/74525270/remove-column-from-2d-array-in-java
              .map(row -> IntStream.range(0, row.size()).filter(i -> i != indexToCheck).mapToObj(row::get).toList()).toList(), D,
          // removing column x from header
          X.stream().filter(col -> !col.equals(x)).toList(), depth - 1);
      // before appending to subtree we need to apply additional info about node/leaf(what produced
      // it)
      if (t instanceof Leaf leaf) {
        leaf.setName(val);
      } else if (t instanceof Node node) {
        node.setParentValue(val);
      }
      subtrees.add(t);
    }

    // create the tree
    Node node = new Node(x, subtrees);
    setTree(node);
    return node;
  }

  public List<String> predict(List<List<String>> data) {
    if (tree == null) {
      throw new RuntimeException("Tree is not fitted");
    } else if (initialHeader == null) {
      throw new RuntimeException("Header is not set");
    }
    List<String> predictions = new ArrayList<>();

    for (List<String> row : data) {
      TreeNode treeNode = tree;
      while (true) {
        // if we reached leaf that mean we found our prediction
        if (treeNode instanceof Leaf leaf) {
          predictions.add(leaf.getValue());
          break;
          // if we found node go deeper
        } else if (treeNode instanceof Node node) {
          int index = initialHeader.indexOf(node.getValue());
          String value = row.get(index);
          for (TreeNode child : node.getChildren()) {
            // geting children and checking if they are leafs
            if (child instanceof Leaf leaf) {
              // getting the correct one
              if (leaf.getName().equals(value)) {
                treeNode = child;
                break;
              }
            } else if (child instanceof Node node1) {
              // getting the correct one
              if (node1.getParentValue().equals(value)) {
                treeNode = child;
                break;
              }
            }
            // if we didnt found match setting it to null so i can get most common
            treeNode = null;
          }
          // not found get most common, then alphabetical from data last column
          if (treeNode == null) {
            treeNode =
                // get last column, get most frequent, then get alphabetical
                new Leaf(data.stream().map(row1 -> row1.get(row1.size() - 1))
                    // https://stackoverflow.com/questions/25441088/how-can-i-count-occurrences-with-groupby
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting())).entrySet()
                    // https://stackoverflow.com/questions/54483094/sorting-a-map-on-first-by-value-then-by-key
                    .stream().min(Map.Entry.<String, Long>comparingByValue().thenComparing(Map.Entry.comparingByKey())).get().getKey());
            ((Leaf) treeNode).setName(value);
          }
        }
      }
    }

    return predictions;
  }

  public double accuracy(List<List<String>> data, List<String> predictions) {
    int correct = 0;
    // taking final value index from data
    int index = data.get(0).size() - 1;
    for (int i = 0; i < data.size(); i++) {
      // compare last column with predictions
      if (data.get(i).get(index).equals(predictions.get(i))) {
        correct++;
      }
    }
    return (double) correct / data.size();
  }

  public String confusionMatrix(List<List<String>> data, List<String> predictions) {
    int index = data.get(0).size() - 1;
    //get set of possible values
    List<String> values = data.stream().map(row -> row.get(index)).sorted().distinct().toList();
    //real class <predicted class> YxY matrix, initialize with 0
    //A <A, B, C>
    //B <A, B, C>
    //C <A, B, C>
    Map<String, Map<String, Integer>> matrix = new LinkedHashMap<>();
    for (String value : values) {
      matrix.put(value, new LinkedHashMap<>());
      for (String value1 : values) {
        matrix.get(value).put(value1, 0);
      }
    }
    //crane game, position by real value on Y axis and then position by predicted value on X axis and +1
    for (int i = 0; i < data.size(); i++) {
      String actual = data.get(i).get(index);
      String predicted = predictions.get(i);
      matrix.get(actual).put(predicted, matrix.get(actual).get(predicted) + 1);
    }

    //formatting output
    String returnValue = "";
    for (String value : values) {
      for (String value1 : values) {
        returnValue += matrix.get(value).get(value1) + " ";
      }
      returnValue += "\n";
    }
    return returnValue;
  }

  // getting set of values in a column
  private Set<String> filterByColumn(List<List<String>> data, List<String> header, String x) {
    int index = header.indexOf(x);

    return data.stream().map(row -> row.get(index)).collect(Collectors.toSet());
  }

  //most common value in a column
  private String argmaxv(List<List<String>> D) {
    HashMap<String, Integer> counts = new HashMap<>();

    int index = D.get(0).size() - 1;
    //TODO: with streams
    for (List<String> row : D) {
      String value = row.get(index);
      if (counts.containsKey(value)) {
        counts.put(value, counts.get(value) + 1);
      } else {
        counts.put(value, 1);
      }
    }
    //return most common, if multiple get alphabeticaly
    return counts.entrySet().stream().sorted(Map.Entry.comparingByKey()).max(Map.Entry.comparingByValue()).get().getKey();
  }

  //choose best feature based on information gain
  private String argmaxx(List<List<String>> D, List<String> X) {
    String maxIGFeature = null;
    double maxIG = -Double.MAX_VALUE;

    for (String x : X) {
      double ig = calculateInformationGain(D, x, X);
      System.out.printf("IG(%s)=%f ", x, ig);

      //if found better feature set it
      if (ig > maxIG) {
        maxIG = ig;
        maxIGFeature = x;
      }
    }
    System.out.println();
    return maxIGFeature;
  }

  private double calculateInformationGain(List<List<String>> D, String x, List<String> X) {
    // x je weather
    double entropy = calculateEntropy(D);
    int index = X.indexOf(x);

    //for each value count appearances
    HashMap<String, Integer> valueCounts = (HashMap<String, Integer>) D.stream().collect(Collectors.groupingBy(row -> row.get(index), Collectors.summingInt(row -> 1)));

    for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
      String value = entry.getKey();
      //pick only rows with value on position index
      List<List<String>> subD = D.stream().collect(Collectors.groupingBy(row -> row.get(index))).get(value);
      //calculate entropy for each value and subtract from total entropy
      entropy -= (double) entry.getValue() / D.size() * calculateEntropy(subD);
    }

    return entropy;
  }

  private double calculateEntropy(List<List<String>> D) {
    //count final accuracies
    HashMap<String, Integer> endValues = (HashMap<String, Integer>) D.stream().collect(Collectors.groupingBy(row -> row.get(row.size() - 1), Collectors.summingInt(row -> 1)));

    double entropy = 0.0;

    for (Map.Entry<String, Integer> entry : endValues.entrySet()) {
      String value = entry.getKey();
      double p = (double) endValues.get(value) / D.size();

      entropy += -(p * Math.log(p)) / Math.log(2);
    }
    return entropy;
  }

  public TreeNode getTree() {
    return tree;
  }

  private void setTree(TreeNode tree) {
    this.tree = tree;
  }

  public boolean isDepthZero() {
    return depthZero;
  }
}
