/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lstm;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Class FragmentedNeuralQueue is an implementation of a neural queue simplified for operation (cannot be used for training) of our bipartite cooperative LSTM architecture
 * @author Nicholas Weintraut
 */
public class FragmentedNeuralQueue {
    private INDArray valueMatrix; //[index][vectorIndex]
    private INDArray strengthVector; //[index]
    private double fragmentStrength;
    private int beginning;
    
    /**
     * Initializes FragmentedNeuralQueue instance with the given input vector size and a fragment strength of 1.
     * @param inputSize 
     */
    public FragmentedNeuralQueue(int inputSize)
    {
        this(inputSize, 1);
    }
   
    /**
     * Initializes FragmentedNeuralQueue instance with the given input vector size and fragment strength
     * @param inputSize
     * @param fragmentStrength 
     */
    public FragmentedNeuralQueue(int inputSize, double fragmentStrength)
    {
        valueMatrix = Nd4j.create(new int[]{0,inputSize});
        this.fragmentStrength = fragmentStrength;
        this.beginning = 0;
    }
    
    /**
     * Perform the next timeStep iteration of the neural queue without dequeueing
     * @param inputVector
     * @param enqueueSignal

     */
    public void enqueueStep(INDArray inputVector, double enqueueSignal)
    {
        //add inputVector to valueMatrix
        valueMatrix.addColumnVector(inputVector);
        
        //we won't update our strengthVector other than adding enqueueSignal
        strengthVector.add(enqueueSignal);
    }
    
    /**
     * Read vectors scaled by their strengths until we fill the fragment, then return the fragment.
     * A vector is scaled by a portion of their strength if their strength would overfill the fragment.
     * @return The sampled vector
     */
    public INDArray peek()
    {
        //lets start generating the readVector
        INDArray readVector = Nd4j.create(valueMatrix.rows());
        double totalStrength = 0.0;
        int i = beginning;
        //while we have not reached the strength limit for our fragment
        while(totalStrength < fragmentStrength && i < strengthVector.length())
        {
            //if our totalStrength would exceed our fragment strength, only take the portion needed to fill it
            if(totalStrength + strengthVector.getDouble(i) > fragmentStrength)
            {
                readVector.add(valueMatrix.getColumn(i).mul(fragmentStrength - totalStrength));
                totalStrength = fragmentStrength;
            }
            //if we have space left, we'll simply multiply the current vector by it's scale and add it.
            else
            {
                readVector.add(valueMatrix.getColumn(i).mul(strengthVector.getDouble(i)));
                totalStrength += strengthVector.getDouble(i);
            }
        }
        
        return readVector;
    }
    
    /**
     * Removes an element of the queue in first-in, first-out order.
     */
    public void dequeueStep()
    {
        strengthVector.putScalar(beginning++, 0.0);
    }
}
