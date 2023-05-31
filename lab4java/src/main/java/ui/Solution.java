package ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class Solution {

  private static String trainFile = "";
  private static String testFile = "";
  private static String nn = "";
  private static int popSize = 0;
  private static int elitism = 0;
  private static double p = 0;
  private static double K = 0;
  private static int iter = 0;
  private static List<List<Object>> trainData = null;
  private static List<String> trainDataHeader = null;
  private static List<Double> trainDataTargetValues = null;
  private static List<List<Object>> testData = null;
  private static List<String> testDataHeader = null;
  private static List<Double> testDataTargetValues = null;

  public static void main(String... args) {

    if (args.length < 16) {
      System.err.println(
          "Expected arguments e.g. --train sine_train.txt --test sine_test.txt --nn 5s --popsize 10 --elitism 1 --p 0.1 --K 0.1 --iter 10000");
      System.exit(1);
    }

    parseInputParams(args);

    NeuralNetwork n = geneticAlgorithm();

    List<Double> output = new ArrayList<>();
    for (List<Object> each : testData) {
      output.add(n.calculate(each.stream().mapToDouble(o -> (double) o).toArray()));
    }
    System.out.println("[Test error]: " + n.calculateError(output.stream().mapToDouble(o -> o).toArray(), testDataTargetValues.stream().mapToDouble(Double::doubleValue).toArray()));
  }

  private static void parseInputParams(String... args) {
    for (int i = 0; i < args.length; i += 2) {
      switch (args[i].strip()) {
        case "--train" -> trainFile = args[i + 1];
        case "--test" -> testFile = args[i + 1];
        case "--nn" -> nn = args[i + 1];
        case "--popsize" -> popSize = Integer.parseInt(args[i + 1]);
        case "--elitism" -> elitism = Integer.parseInt(args[i + 1]);
        case "--p" -> p = Double.parseDouble(args[i + 1]);
        case "--K" -> K = Double.parseDouble(args[i + 1]);
        case "--iter" -> iter = Integer.parseInt(args[i + 1]);
        default -> throw new IllegalStateException("Unexpected value: " + args[i]);
      }
    }

//    System.out.println("trainFile = " + trainFile);
//    System.out.println("testFile = " + testFile);
//    System.out.println("nn = " + nn);
//    System.out.println("popSize = " + popSize);
//    System.out.println("elitism = " + elitism);
//    System.out.println("p = " + p);
//    System.out.println("K = " + K);
//    System.out.println("iter = " + iter);

    trainData = parseCSV(trainFile);
    //remove first row and remove last element since its output and i want to keep header.size == data.size
    trainDataHeader =
        trainData.remove(0).stream().limit(trainData.get(0).size()-1).map(Objects::toString).toList();
    trainDataTargetValues = trainData.stream()
        .map(row -> (Double) row.remove(row.size() - 1))
        .toList();

    testData = parseCSV(testFile);
    testDataHeader =
        testData.remove(0).stream().limit(testData.get(0).size()-1).map(Objects::toString).toList();
    testDataTargetValues =
        testData.stream()
            .map(row -> (Double) row.remove(row.size() - 1))
            .toList();

//    System.out.println("trainDataHeader = " + trainDataHeader);
//    System.out.println("trainData = " + trainData);
//    System.out.println("trainTargetValues = " + trainDataTargetValues);
//    System.out.println("testDataHeader = " + testDataHeader);
//    System.out.println("testData = " + testData);
//    System.out.println("testTargetValues = " + testDataTargetValues);
  }

  private static List<List<Object>> parseCSV(String csvfile) {
    List<List<Object>> data = new ArrayList<>();

    // regular parsing by ","
    try (Scanner scanner = new Scanner(new File(csvfile))) {
      while (scanner.hasNextLine()) {
        data.add(
            Arrays.stream(scanner.nextLine().split(","))
                .map(String::strip)
                .map(
                    element -> {
                      try {
                        return Double.parseDouble(element);
                      } catch (NumberFormatException e) {
                        return element; // return the original string if it's not parsable to Double
                      }
                    })
                .collect(Collectors.toList()));
      }

    } catch (FileNotFoundException e) {
      System.out.println("File not found: " + csvfile);
    }
    return data;
  }

  private static int[] parseNNInput(String nn) {
    String[] nnSplit = nn.split("s");
    int[] hiddenLayers = new int[nnSplit.length];
    for (int i = 0; i < nnSplit.length; i++) {
      hiddenLayers[i] = Integer.parseInt(nnSplit[i]);
    }
    return hiddenLayers;
  }

  //psudocode from ppt
  private static NeuralNetwork geneticAlgorithm() {
    //create initial population of nn's
    var P = new ArrayList<NeuralNetwork>(popSize);
    for (int i = 0; i < popSize; i++) {
      P.add(new NeuralNetwork(trainDataHeader.size(), parseNNInput(nn)));
    }

    evaluate(P);

    //for plotting
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    for (int i = 0; i <= iter; i++) {
      //for each iteration we have new population
      var Px = new ArrayList<NeuralNetwork>(popSize);
      //sort by fitness
      P.sort((n1, n2) -> {
        double fitness1 = fitnessFromError(n1.getError());
        double fitness2 = fitnessFromError(n2.getError());
        return Double.compare(fitness2, fitness1);
      });
      //plotting
      dataset.addValue(P.get(0).getError(), "Train Error", String.valueOf(i));

      if (i != 0 && i % 2000 == 0) {
        System.out.println("[Train error @" + i + "]: " + P.get(0).getError());
      }
      //always keep the N best
      for (int j = 0; j < elitism; j++) {
        Px.add(P.get(j));
      }
      //fill with the rest crossed and mutated
      for (int j = 0; j < popSize-elitism; j++) {
        NeuralNetwork n1 = fitnessProportionalSelection(P);
        NeuralNetwork n2 = fitnessProportionalSelection(P);
        NeuralNetwork d1 = cross(n1, n2);
        d1 = mutate(d1);
        Px.add(d1);
      }
      P = Px;
      evaluate(P);
    }

    plotChart(dataset);

    //return the best
    P.sort((n1, n2) -> {
      double fitness1 = fitnessFromError(n1.getError());
      double fitness2 = fitnessFromError(n2.getError());
      return Double.compare(fitness2, fitness1);
    });
    return P.get(0);
  }

  //copy paste from internet how to draw chart in java
  private static void plotChart(DefaultCategoryDataset dataset) {
    JFreeChart chart = ChartFactory.createLineChart(
        "GA Progress",
        "Iteration",
        "Error",
        dataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false
    );

    JFrame frame = new JFrame("GA Progress Chart");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(new ChartPanel(chart));
    frame.pack();
    frame.setVisible(true);
  }

  //performing mutation
  private static NeuralNetwork mutate(NeuralNetwork n) {
    Random random = new Random();
    NeuralNetwork mutated = new NeuralNetwork(n);
    //get weights from 3d array
    for(int i = 0; i < mutated.getNeuronWeights().length; i++) {
      for(int j = 0; j < mutated.getNeuronWeights()[i].length; j++) {
        for(int k = 0; k < mutated.getNeuronWeights()[i][j].length; k++) {
          //probability of mutation
          if(random.nextDouble() < p) {
            //Gaussan noise with std dev K added to old weight
            mutated.getNeuronWeights()[i][j][k] += K * random.nextGaussian();
          }
        }
      }
    }
    return mutated;
  }

  //crossing 2 nn's
  private static NeuralNetwork cross(NeuralNetwork n1, NeuralNetwork n2) {
    NeuralNetwork child = new NeuralNetwork(n1);
    for(int i = 0; i < child.getNeuronWeights().length; i++) {
      for(int j = 0; j < child.getNeuronWeights()[i].length; j++) {
        for(int k = 0; k < child.getNeuronWeights()[i][j].length; k++) {
          //arithmetic mean of both weights
          child.getNeuronWeights()[i][j] = arithmeticMean(n1.getNeuronWeights()[i][j], n2.getNeuronWeights()[i][j]);
        }
      }
    }
    return child;
  }

  //weights is array for AM is mean of each one
  private static double[] arithmeticMean(double[] n1, double[] n2) {
    double[] mean = new double[n1.length];
    for (int i = 0; i < n1.length; i++) {
      mean[i] = (n1[i] + n2[i]) / 2;
    }
    return mean;
  }

  //for each nn in P, for each line in file, calculate the output, error and set the error
  private static void evaluate(List<NeuralNetwork> P) {
    for (NeuralNetwork neuralNetwork : P) {
      List<Double> dataOutput = new ArrayList<>();
      for (List<Object> each : trainData) {
        dataOutput.add(neuralNetwork.calculate(each.stream().mapToDouble(o -> (double) o).toArray()));
      }
      double error = neuralNetwork.calculateError(
          dataOutput.stream().mapToDouble(Double::doubleValue).toArray(),
          trainDataTargetValues.stream().mapToDouble(Double::doubleValue).toArray()
      );
      neuralNetwork.setError(error);
    }
  }

  //roulette wheel selection from ppt/script
  private static NeuralNetwork fitnessProportionalSelection(List<NeuralNetwork> P) {
    double sum = 0;
    Random random = new Random();
    for (NeuralNetwork neuralNetwork : P) {
      sum += fitnessFromError(neuralNetwork.getError());
    }

    double x = random.nextDouble() * sum;

    double area = 0;

    for (NeuralNetwork neuralNetwork : P) {
      area += fitnessFromError(neuralNetwork.getError());
      if (x <= area) {
        return neuralNetwork;
      }
    }
    return null;
  }

  private static double fitnessFromError(double error) {
    return 1 / error;
  }
}
