/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import lstm.LSTMNode;
import lstm.FragmentedNeuralQueue;
/**
 *
 * @author cssummer16
 */
public class CompressingAutoencoder {
    
    public static void main(String[] args)
    {
        int steps = 20;
        int inputSize = 24;
        //There is an error with Nd4j furing multiplication of weights and input when the input size on an LSTM node is greater than the output size
        //geez
        int featureVectorSize = 24;
        int outputSize = 24;
        INDArray inputSequence = Nd4j.rand(10, inputSize);
        INDArray lstmWeights1 = Nd4j.rand(new int[]{4, featureVectorSize + 1, inputSize});
        INDArray lstmBiases1 = Nd4j.rand(new int[]{4, featureVectorSize + 1});
        LSTMNode encoder = new LSTMNode(inputSize, featureVectorSize + 1, lstmWeights1, lstmBiases1);
        FragmentedNeuralQueue fragQueue = new FragmentedNeuralQueue(featureVectorSize);
        INDArray lstmWeights2 = Nd4j.rand(new int[]{4, outputSize, featureVectorSize});
        INDArray lstmBiases2 = Nd4j.rand(new int[]{4, outputSize});
        LSTMNode decoder = new LSTMNode(featureVectorSize, outputSize, lstmWeights2, lstmBiases2);
    
        System.out.println("STARTED");
        for(int i = 0; i < inputSequence.columns(); i++)
        {
            INDArray lstmOut1 = encoder.step(inputSequence.slice(0));
            System.out.println("whyyyyyyy");
            INDArray lstm1Vector = Nd4j.create(lstmOut1.length() - 1);
            for(int j = 0; j < lstm1Vector.length(); j++)
            {
                lstm1Vector.putScalar(j, lstmOut1.getDouble(j));
            }
            System.out.println("here we go queue!!!");
            fragQueue.enqueueStep(lstm1Vector, lstmOut1.getDouble(lstmOut1.length() - 1));
            System.out.println("we queue'd!");
        }
        
        for(int i = 0; i < inputSequence.columns(); i++)
        {
            System.out.println("AAAHHHHHH");
            
            System.out.println(decoder.step(fragQueue.peek()));
            fragQueue.dequeueStep();
        }
    }
}
