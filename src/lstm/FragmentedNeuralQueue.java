/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lstm;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * Class FragmentedNeuralQueue is an implementation of a neural queue simplified for operation (cannot be used for training) of our bipartite cooperative LSTM architecture
 * @author Nicholas Weintraut
 */
public class FragmentedNeuralQueue {
    private LinkedList<INDArray> vectorList; //[index][vectorIndex]
    private LinkedList<Double> strengthList; //[index]
    private double fragmentStrength;
    private int inputSize;
    private Double totalStrength;
    
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
        vectorList = new LinkedList<INDArray>(); 
        strengthList = new LinkedList<Double>();
        this.fragmentStrength = fragmentStrength;
        this.inputSize = inputSize;
        totalStrength = 0.0;
    }
    
    /**
     * Perform the next timeStep iteration of the neural queue without dequeueing
     * @param inputVector
     * @param enqueueSignal

     */
    public void enqueueStep(INDArray inputVector, double enqueueSignal)
    {
        //System.out.println("are we gonna do it");
        //add inputVector to valueMatrix
        //System.out.println(inputVector == null);
        vectorList.add(inputVector);
        //System.out.println("hurr");
        //System.out.println("we added column vector, now lets add an enqueueSignal");
        //we won't update our strengthVector other than adding enqueueSignal
       // System.out.println(strengthList == null);
        
        strengthList.add(enqueueSignal);
        
        totalStrength += enqueueSignal;
    }
    
    /**
     * 
     * @return Does the queue have 
     */
    public boolean hasFullBuffer()
    {
        return totalStrength >= fragmentStrength;
    }
    
    /**
     * Read vectors scaled by their strengths until we fill the fragment, then return the fragment.
     * A vector is scaled by a portion of their strength if their strength would overfill the fragment.
     * @return The sampled vector
     */
    public INDArray peek()
    {
        //lets start generating the readVector
        INDArray readVector = Nd4j.create(inputSize);
        
        double strengthSum = 0.0;
        
        Iterator<INDArray> vectorIterator = vectorList.iterator();
        Iterator<Double> strengthIterator = strengthList.iterator();
        //while we have not reached the strength limit for our fragment
        while(strengthSum < fragmentStrength && vectorIterator.hasNext())
        {
            INDArray currVector = vectorIterator.next();
            //System.out.println(currVector.length());
            double currStrength = strengthIterator.next();
            //if our strengthSum would exceed our fragment strength, only take the portion needed to fill it
            if(strengthSum + currStrength > fragmentStrength)
            {
                readVector.add(currVector.mul(fragmentStrength - strengthSum));
                strengthSum = fragmentStrength;
            }
            //if we have space left, we'll simply multiply the current vector by it's scale and add it.
            else
            {
                readVector.add(currVector.mul(currStrength));
                strengthSum += currStrength;
            }
        }
        
        return readVector;
    }
    
    /**
     * Removes an element of the queue in first-in, first-out order.
     */
    public INDArray dequeueStep()
    {
        INDArray result = this.peek();
        vectorList.remove(0);
        totalStrength -= strengthList.remove(0);
        return result;
    }
}
