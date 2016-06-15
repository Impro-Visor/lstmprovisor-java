/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lstm;

import org.nd4j.linalg.api.ndarray.*;
import org.nd4j.linalg.factory.Nd4j;

/**
 *  Class NeuralQueue implements a neural queue data structure as described in Google DeepMind's paper, "Learning to Transduce with Unbounded Memory"
 *  Credit to iamtrask for his explanation and analysis of the paper, "How to Code and Understand DeepMind's Neural Stack Machine"
 * @author Nicholas Weintraut
 */
public class NeuralQueue {
    
    
    private INDArray valueMatrix; //[index][vectorIndex]
    private INDArray strengthVector; //[index]
    
    public NeuralQueue(int inputSize)
    {
        valueMatrix = Nd4j.create(new int[]{0, inputSize});
        strengthVector = Nd4j.create(inputSize);
    }
   
    /**
     * Perform the next timeStep iteration of the neural queue
     * @param inputVector
     * @param enqueueSignal
     * @param dequeueSignal
     * @return 
     */
    public INDArray step(INDArray inputVector, double enqueueSignal, double dequeueSignal)
    {
        //add inputVector to valueMatrix
        valueMatrix.addColumnVector(inputVector);
        //re-calculate strengthVector
        updateStrengthVector(enqueueSignal, dequeueSignal);
        
        //lets start generating the readVector
        INDArray readVector = Nd4j.create(inputVector.length());
        for(int i = 0; i < valueMatrix.columns(); i++)
        {
            //sum our strengthVectors for every vector before the added vector
            double strengthSum = 0.0;
            for(int j = 0; j < i; j++)
            {
                strengthSum += strengthVector.getDouble(j);
            }
            //subtract that value from 1
            double resultStrength = 1 - strengthSum;
            //find max of resultStrength and zero
            resultStrength = (resultStrength > 0) ? resultStrength : 0.0;
            //find min of the most recent strength and resultStrength
            resultStrength = (strengthVector.getDouble(i) < resultStrength) ? strengthVector.getDouble(i) : resultStrength;
            //scale the current vector of the valueMatrix by our resultStrength and add it to readVector
            readVector.add(valueMatrix.getColumn(i).muli(resultStrength));
        }
        
        return readVector;
    }
    
    /**
     * Updates the values in the strength vector with the 
     * @param dequeueSignal 
     */
    private void updateStrengthVector(double enqueueSignal, double dequeueSignal)
    {
        INDArray newStrengths = Nd4j.create(strengthVector.length()+1);
        
        for(int i = 0; i < newStrengths.length() - 1; i++)
        {
            //sum the strength values for all previous positions
            double value1 = 0.0;
            for(int j = 0; j < i; j++)
            {
                value1 += strengthVector.getDouble(j);
            }
            //subtract our sum of strengths from our dequeueSignal
            double value2 = dequeueSignal - value1;
            //bound it to above zero
            value2 = (value2 > 0) ? value2 : 0;
            //get the new strength by subtracting the altered signal from our current strength
            double value3 = strengthVector.getDouble(i) - value2;
            //bound it to above zero
            value3 = (value3 > 0) ? value3 : 0;
            //set the value
            newStrengths.putScalar(i, value3);
        }
        
        //set our added item's value as the enqueueSignal
        newStrengths.putScalar(newStrengths.length() - 1, enqueueSignal);
        //update strengthVector to reference our temporary INDArray
        strengthVector = newStrengths;
    }
    
    
}
