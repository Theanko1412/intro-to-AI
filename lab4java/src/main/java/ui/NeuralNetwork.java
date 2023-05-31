package ui;

import java.util.Arrays;
import java.util.Random;

public class NeuralNetwork {

  public int inputSize;
  public int outputSize = 1;
  public int[] hiddenLayers;
  public double error = 0;

  private double[][] neuronOutput;  //layer x neuron
  private double[][][] neuronWeights;   //layer x neuron x previous layer neuron
  private double[][] neuronBias;    //layer x neuron

  //creating new neural network with random weights
  public NeuralNetwork(int inputSize, int[] hiddenLayers) {
    this.inputSize = inputSize;
    //initializing hidden layers arrays with additional output layer
    this.hiddenLayers = new int[hiddenLayers.length + 1];
    System.arraycopy(hiddenLayers, 0, this.hiddenLayers, 0, hiddenLayers.length);
    this.hiddenLayers[hiddenLayers.length] = outputSize;

    //for each layer we will have array of neurons
    this.neuronOutput = new double[this.hiddenLayers.length][];
    this.neuronWeights = new double[this.hiddenLayers.length][][];
    this.neuronBias = new double[this.hiddenLayers.length][];

    Random rand = new Random();

    //putting random weights and biases
    //for each hidden layer
    for (int layerIndex = 0; layerIndex < this.hiddenLayers.length; layerIndex++) {
      //for each layer we will have provided N neurons
      this.neuronOutput[layerIndex] = new double[this.hiddenLayers[layerIndex]];
      this.neuronWeights[layerIndex] = new double[this.hiddenLayers[layerIndex]][];
      this.neuronBias[layerIndex] = new double[this.hiddenLayers[layerIndex]];

      //for each neuron set random weights
      for (int neuronIndex = 0; neuronIndex < this.hiddenLayers[layerIndex]; neuronIndex++) {
        //first layer is connected to input vector so number of weights is equal to input vector size else it is equal to previous layer size
        this.neuronWeights[layerIndex][neuronIndex] = new double[layerIndex == 0 ? inputSize : this.hiddenLayers[layerIndex - 1]];
        for (int weightIndex = 0; weightIndex < this.neuronWeights[layerIndex][neuronIndex].length; weightIndex++) {
          // normal distribution with standard deviation of 0.01
          this.neuronWeights[layerIndex][neuronIndex][weightIndex] = rand.nextGaussian() * 0.01;
        }
        this.neuronBias[layerIndex][neuronIndex] = rand.nextGaussian() * 0.01;
      }
    }
  }

  //creating neural network from existing one, needed for crossover/mutation
  public NeuralNetwork(NeuralNetwork neuralNetwork) {
    this.inputSize = neuralNetwork.inputSize;
    this.outputSize = neuralNetwork.outputSize;
    this.hiddenLayers = new int[neuralNetwork.hiddenLayers.length];
    System.arraycopy(neuralNetwork.hiddenLayers, 0, this.hiddenLayers, 0, neuralNetwork.hiddenLayers.length);

    this.neuronOutput = new double[this.hiddenLayers.length][];
    this.neuronWeights = new double[this.hiddenLayers.length][][];
    this.neuronBias = new double[this.hiddenLayers.length][];

    for (int layerIndex = 0; layerIndex < this.hiddenLayers.length; layerIndex++) {
      this.neuronOutput[layerIndex] = new double[this.hiddenLayers[layerIndex]];
      this.neuronWeights[layerIndex] = new double[this.hiddenLayers[layerIndex]][];
      this.neuronBias[layerIndex] = new double[this.hiddenLayers[layerIndex]];

      for (int neuronIndex = 0; neuronIndex < this.hiddenLayers[layerIndex]; neuronIndex++) {
        this.neuronWeights[layerIndex][neuronIndex] = new double[layerIndex == 0 ? inputSize : this.hiddenLayers[layerIndex - 1]];
        System.arraycopy(neuralNetwork.neuronWeights[layerIndex][neuronIndex], 0, this.neuronWeights[layerIndex][neuronIndex], 0, this.neuronWeights[layerIndex][neuronIndex].length);
        this.neuronBias[layerIndex][neuronIndex] = neuralNetwork.neuronBias[layerIndex][neuronIndex];
      }
    }
  }

  //get output from neural network
  public double calculate(double[] input) {
    //for each layer and each neuron calculate output
    for (int layerIndex = 0; layerIndex < hiddenLayers.length; layerIndex++) {
      for (int neuronIndex = 0; neuronIndex < hiddenLayers[layerIndex]; neuronIndex++) {
        double sum = 0;
        for (int previousNeuronIndex = 0; previousNeuronIndex < (layerIndex == 0 ? inputSize : hiddenLayers[layerIndex - 1]); previousNeuronIndex++) {
          //again we need to check if it is first layer or not because first is connected to input vector
          sum += neuronWeights[layerIndex][neuronIndex][previousNeuronIndex] * (layerIndex == 0 ? input[previousNeuronIndex] : neuronOutput[layerIndex - 1][previousNeuronIndex]);
        }
        //add neuron bias to sum as well
        sum += neuronBias[layerIndex][neuronIndex];
        //if it is last layer we don't need to apply sigmoid function
        if(layerIndex == hiddenLayers.length - 1) {
          neuronOutput[layerIndex][neuronIndex] = sum;
        } else {
          neuronOutput[layerIndex][neuronIndex] = sigmoid(sum);
        }
      }
    }
    //return output from last layer
    return neuronOutput[hiddenLayers.length - 1][0];
  }

  //mse from output vs expectedOutput
  public double calculateError(double[] output, double[] expectedOutput) {
    double mse = 0.0;
    for(int i = 0; i < output.length; i++) {
      mse += Math.pow(expectedOutput[i]- output[i], 2);
    }
    return mse/output.length;
  }

  private double sigmoid(double sum) {
    return 1 / (1 + Math.exp(-sum));
  }

  public double[][][] getNeuronWeights() {
    return neuronWeights;
  }

  public double getError() {
    return error;
  }

  public void setError(double error) {
    this.error = error;
  }

  @Override
  public String toString() {
    return "NeuralNetwork{" +
        "inputSize=" + inputSize +
        ", outputSize=" + outputSize +
        ", hiddenLayers=" + Arrays.toString(hiddenLayers) +
        ", neuronOutput=" + Arrays.deepToString(neuronOutput) +
        ", neuronWeights=" + Arrays.deepToString(neuronWeights) +
        ", neuronBias=" + Arrays.deepToString(neuronBias) +
        ", error=" + error +
        '}';
  }
}
